package com.example.chat_app.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.caffeine.CaffeineProxyManager;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Rate Limiting Configuration
 * يستخدم Bucket4j مع Caffeine Cache لحماية التطبيق من هجمات DoS
 */
@Configuration
public class RateLimitingConfig {

    /**
     * إنشاء ProxyManager للـ Rate Limiting
     * يستخدم Caffeine Cache لتخزين البيانات
     */
    @Bean
    public ProxyManager<String> proxyManager() {
        return new CaffeineProxyManager<>(
                Caffeine.newBuilder()
                        .maximumSize(10_000), // حد أقصى 10,000 مستخدم
                Duration.ofMinutes(1) // فترة الـ keepAfterRefill - المدة التي يتم الاحتفاظ بالـ bucket بعد آخر refill
        );
    }

    /**
     * إنشاء Bucket Configuration الافتراضي
     * يسمح بـ 100 طلب في الدقيقة لكل مستخدم
     */
    @Bean
    public Supplier<BucketConfiguration> bucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(100).refillIntervally(100, Duration.ofMinutes(1))) // 100 طلب/دقيقة
                .build();
    }

    /**
     * Bucket Configuration للـ Authentication Endpoints
     * أكثر صرامة: 10 محاولات في الدقيقة
     */
    @Bean
    public Supplier<BucketConfiguration> authBucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(10).refillIntervally(10, Duration.ofMinutes(1))) // 10 طلبات/دقيقة
                .build();
    }

    /**
     * Bucket Configuration للـ Message Endpoints
     * متوسط: 50 رسالة في الدقيقة
     */
    @Bean
    public Supplier<BucketConfiguration> messageBucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(50).refillIntervally(50, Duration.ofMinutes(1))) // 50 رسالة/دقيقة
                .build();
    }

    /**
     * Bucket Configuration للـ Public Endpoints
     * أكثر تساهلاً: 200 طلب في الدقيقة
     */
    @Bean
    public Supplier<BucketConfiguration> publicBucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(200).refillIntervally(200, Duration.ofMinutes(1))) // 200 طلب/دقيقة
                .build();
    }
}
