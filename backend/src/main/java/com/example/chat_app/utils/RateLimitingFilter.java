package com.example.chat_app.utils;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Rate Limiting Filter
 * يطبق Rate Limiting على جميع الطلبات لحماية التطبيق من هجمات DoS
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ProxyManager<String> proxyManager;
    private final Supplier<BucketConfiguration> bucketConfiguration;
    private final Supplier<BucketConfiguration> authBucketConfiguration;
    private final Supplier<BucketConfiguration> messageBucketConfiguration;
    private final Supplier<BucketConfiguration> publicBucketConfiguration;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // الحصول على IP Address للمستخدم
        String clientIp = getClientIp(request);
        String requestUri = request.getRequestURI();

        // اختيار Bucket Configuration المناسب حسب نوع الـ Endpoint
        Supplier<BucketConfiguration> selectedConfig = selectBucketConfiguration(requestUri);

        // إنشاء مفتاح فريد للمستخدم (IP + URI)
        String bucketKey = clientIp + ":" + requestUri;

        // الحصول على Bucket للمستخدم
        Bucket bucket = proxyManager.builder().build(bucketKey, selectedConfig);

        // محاولة استهلاك Token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // إضافة Headers للـ Response
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X-Rate-Limit-Retry-After-Seconds",
                    String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));

            // السماح بالطلب
            filterChain.doFilter(request, response);
        } else {
            // رفض الطلب - تجاوز الحد المسموح
            log.warn("Rate limit exceeded for IP: {} on URI: {}", clientIp, requestUri);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.addHeader("X-Rate-Limit-Retry-After-Seconds",
                    String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));

            String jsonResponse = String.format(
                    "{\"error\": \"Too many requests\", \"message\": \"Rate limit exceeded. Please try again in %d seconds.\"}",
                    probe.getNanosToWaitForRefill() / 1_000_000_000);
            response.getWriter().write(jsonResponse);
        }
    }

    /**
     * اختيار Bucket Configuration المناسب حسب نوع الـ Endpoint
     */
    private Supplier<BucketConfiguration> selectBucketConfiguration(String requestUri) {
        // Authentication endpoints - أكثر صرامة
        if (requestUri.startsWith("/api/auth/")) {
            return authBucketConfiguration;
        }

        // Message endpoints - متوسط
        if (requestUri.startsWith("/api/messages/")) {
            return messageBucketConfiguration;
        }

        // Public endpoints - أكثر تساهلاً
        if (requestUri.startsWith("/api/public/")) {
            return publicBucketConfiguration;
        }

        // Default configuration
        return bucketConfiguration;
    }

    /**
     * الحصول على IP Address الحقيقي للمستخدم
     * يأخذ في الاعتبار Proxy و Load Balancers
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * استثناء بعض الـ Endpoints من Rate Limiting
     * مثل Health Check و Actuator Endpoints
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") ||
                path.startsWith("/health") ||
                path.equals("/");
    }
}
