# Rate Limiting Implementation - حماية من هجمات DoS

## نظرة عامة

تم تطبيق **Rate Limiting** في المشروع باستخدام **Bucket4j** مع **Caffeine Cache** لحماية التطبيق من هجمات **DoS (Denial of Service)** و **DDoS**.

## المكونات الرئيسية

### 1. Dependencies (pom.xml)

```xml
<!-- Rate Limiting with Bucket4j -->
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-caffeine</artifactId>
    <version>8.7.0</version>
</dependency>
```

### 2. RateLimitingConfig.java

Configuration class يحتوي على:

- **ProxyManager**: يدير الـ Buckets باستخدام Caffeine Cache
- **Bucket Configurations**: إعدادات مختلفة لكل نوع من الـ endpoints

#### إعدادات Rate Limiting

| نوع الـ Endpoint | الحد الأقصى | الفترة الزمنية | الاستخدام |
|-----------------|-------------|----------------|-----------|
| **Default** | 100 طلب | دقيقة واحدة | جميع الـ endpoints الافتراضية |
| **Authentication** | 10 طلبات | دقيقة واحدة | `/api/auth/**` - حماية من Brute Force |
| **Messages** | 50 رسالة | دقيقة واحدة | `/api/messages/**` - منع Spam |
| **Public** | 200 طلب | دقيقة واحدة | `/api/public/**` - endpoints عامة |

### 3. RateLimitingFilter.java

Filter يطبق Rate Limiting على جميع الطلبات:

#### الميزات الرئيسية:

1. **IP-based Rate Limiting**: يتتبع الطلبات حسب IP Address
2. **Endpoint-specific Limits**: حدود مختلفة حسب نوع الـ endpoint
3. **Proxy Support**: يدعم X-Forwarded-For و X-Real-IP headers
4. **Response Headers**: يضيف معلومات عن الحد المتبقي
5. **Exclusions**: استثناء بعض الـ endpoints (health checks, actuator)

#### Response Headers

عند كل طلب، يتم إضافة:

```
X-Rate-Limit-Remaining: <عدد الطلبات المتبقية>
X-Rate-Limit-Retry-After-Seconds: <الوقت حتى التجديد>
```

#### Response عند تجاوز الحد

```json
{
  "error": "Too many requests",
  "message": "Rate limit exceeded. Please try again in X seconds."
}
```

**HTTP Status**: `429 Too Many Requests`

### 4. SecurityConfig.java

تم إضافة `RateLimitingFilter` إلى Security Filter Chain:

```java
.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
```

**ترتيب الـ Filters**:
1. RateLimitingFilter (أول filter - يفحص الحد قبل أي شيء)
2. JwtAuthenticationFilter
3. UsernamePasswordAuthenticationFilter

## كيفية الاستخدام

### تخصيص الحدود

لتغيير حدود Rate Limiting، عدّل في `RateLimitingConfig.java`:

```java
@Bean
public Supplier<BucketConfiguration> bucketConfiguration() {
    return () -> BucketConfiguration.builder()
            .addLimit(limit -> limit
                .capacity(100)  // عدد الطلبات
                .refillIntervally(100, Duration.ofMinutes(1))) // فترة التجديد
            .build();
}
```

### إضافة حدود جديدة لـ endpoint معين

1. أضف Bean جديد في `RateLimitingConfig.java`:

```java
@Bean
public Supplier<BucketConfiguration> customBucketConfiguration() {
    return () -> BucketConfiguration.builder()
            .addLimit(limit -> limit.capacity(30).refillIntervally(30, Duration.ofMinutes(1)))
            .build();
}
```

2. عدّل `selectBucketConfiguration()` في `RateLimitingFilter.java`:

```java
if (requestUri.startsWith("/api/custom/")) {
    return customBucketConfiguration;
}
```

### استثناء endpoint من Rate Limiting

عدّل `shouldNotFilter()` في `RateLimitingFilter.java`:

```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/actuator/") ||
           path.startsWith("/health") ||
           path.startsWith("/api/public/status") || // endpoint جديد
           path.equals("/");
}
```

## اختبار Rate Limiting

### باستخدام Postman

1. أرسل طلبات متكررة لنفس الـ endpoint
2. راقب الـ Response Headers:
   - `X-Rate-Limit-Remaining`
   - `X-Rate-Limit-Retry-After-Seconds`
3. بعد تجاوز الحد، ستحصل على:
   - Status: `429 Too Many Requests`
   - رسالة توضح متى يمكنك المحاولة مرة أخرى

### باستخدام cURL

```bash
# إرسال 15 طلب متتالي لـ authentication endpoint (الحد 10)
for i in {1..15}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"test"}' \
    -v
done
```

### باستخدام Script

```bash
# PowerShell
1..15 | ForEach-Object {
    Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body '{"username":"test","password":"test"}' `
        -Verbose
}
```

## الفوائد الأمنية

### 1. حماية من DoS/DDoS
- منع المهاجمين من إغراق السيرفر بالطلبات
- الحد من استهلاك الموارد

### 2. حماية من Brute Force
- حدود صارمة على authentication endpoints
- منع محاولات تخمين كلمات المرور

### 3. منع Spam
- حدود على إرسال الرسائل
- منع إساءة استخدام الـ API

### 4. Fair Usage
- ضمان توزيع عادل للموارد بين المستخدمين
- منع استهلاك مستخدم واحد لكل الموارد

## Best Practices

### 1. مراقبة الـ Logs

راقب الـ logs للكشف عن محاولات الهجوم:

```
WARN: Rate limit exceeded for IP: 192.168.1.100 on URI: /api/auth/login
```

### 2. تعديل الحدود حسب الحاجة

- راقب استخدام الـ API
- عدّل الحدود بناءً على الاستخدام الفعلي
- استخدم حدود أكثر صرامة للـ endpoints الحساسة

### 3. استخدام Redis للـ Production

للـ Production environments مع multiple servers، استخدم Redis بدلاً من Caffeine:

```xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-redis</artifactId>
    <version>8.7.0</version>
</dependency>
```

### 4. Whitelist للـ IPs الموثوقة

أضف whitelist للـ IPs الموثوقة (مثل monitoring services):

```java
private static final Set<String> WHITELISTED_IPS = Set.of(
    "127.0.0.1",
    "::1"
);

@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String clientIp = getClientIp(request);
    return WHITELISTED_IPS.contains(clientIp) || 
           request.getRequestURI().startsWith("/actuator/");
}
```

## Troubleshooting

### المشكلة: Rate Limiting لا يعمل

**الحل**:
1. تأكد من أن `RateLimitingFilter` مضاف في `SecurityConfig`
2. تحقق من ترتيب الـ Filters
3. راجع الـ logs للتأكد من عدم وجود exceptions

### المشكلة: جميع المستخدمين يشاركون نفس الحد

**الحل**:
- تأكد من أن `getClientIp()` يعمل بشكل صحيح
- إذا كنت خلف Proxy، تأكد من إرسال `X-Forwarded-For` header

### المشكلة: الحد يُعاد بسرعة كبيرة

**الحل**:
- عدّل `expireAfterWrite` في `ProxyManager`
- تأكد من أن `Duration.ofMinutes(1)` صحيح

## الخلاصة

تم تطبيق نظام Rate Limiting شامل يحمي التطبيق من:
- ✅ هجمات DoS/DDoS
- ✅ Brute Force Attacks
- ✅ API Abuse
- ✅ Resource Exhaustion

النظام قابل للتخصيص ويدعم حدود مختلفة لكل نوع من الـ endpoints.
