# 🔒 Access Denied (403) Troubleshooting Guide

## 📋 نظرة عامة
هذا الدليل يوضح جميع الأسباب المحتملة لمشكلة **Access Denied (403 Forbidden)** في تطبيق Chat-App.

---

## 🎯 الـ Middleware المسببة للمشكلة

### **1️⃣ JwtAuthenticationFilter** ⚠️⚠️⚠️ **الأكثر احتمالاً**

**المسار:** `src/main/java/com/example/chat_app/utils/JwtAuthenticationFilter.java`

#### **الأسباب المحتملة:**

##### **أ) Token منتهي الصلاحية** ⏰
```
الخطأ في الـ Log:
❌ JWT Token Expired for path: /api/... | Error: JWT expired at ...
```

**السبب:**
- الـ token تم إنشاؤه منذ فترة طويلة
- مدة صلاحية الـ token (في `application.properties`):
  ```properties
  application.security.jwt.expiration=86400000  # 24 hours
  ```

**الحل:**
1. احصل على token جديد من `/api/auth/login`
2. أو زيادة مدة الصلاحية في `application.properties`

---

##### **ب) Token غير موجود أو Format خاطئ** 🚫
```
الخطأ في الـ Log:
=== No Bearer Token found for: /api/...
```

**السبب:**
- لم يتم إرسال `Authorization` header
- أو الـ format خاطئ

**الصيغة الصحيحة:**
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**الصيغ الخاطئة:**
```http
❌ Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
❌ Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
❌ Token: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**الحل:**
- تأكد من إضافة `Authorization: Bearer <token>` في كل request

---

##### **ج) Token معطوب (Malformed)** 🔨
```
الخطأ في الـ Log:
❌ Malformed JWT Token for path: /api/... | Error: ...
```

**السبب:**
- الـ token تم قصه أو تعديله
- الـ token غير مكتمل
- الـ token يحتوي على أحرف غير صحيحة

**الحل:**
- تأكد من نسخ الـ token كاملاً
- لا تضيف مسافات أو أحرف إضافية

---

##### **د) Secret Key خاطئ** 🔑
```
الخطأ في الـ Log:
❌ Invalid JWT Signature (wrong secret key?) for path: /api/... | Error: ...
```

**السبب:**
- الـ token تم إنشاؤه بـ secret key مختلف
- تم تغيير الـ secret key في `application.properties`

**الحل:**
- تأكد من أن الـ secret key في `application.properties` هو نفسه المستخدم لإنشاء الـ token
- احصل على token جديد بعد تغيير الـ secret key

---

##### **هـ) Username غير موجود** 👤
```
الخطأ في الـ Log:
❌ JWT Authentication Error for path: /api/... | Error: UsernameNotFoundException: ...
```

**السبب:**
- الـ username في الـ token غير موجود في قاعدة البيانات
- تم حذف المستخدم

**الحل:**
- تأكد من أن المستخدم موجود في قاعدة البيانات
- احصل على token جديد لمستخدم موجود

---

##### **و) Username في Token لا يطابق UserDetails** 🔄
```
الخطأ في الـ Log:
❌ JWT Invalid (expired or username mismatch) for: ... at path: /api/...
```

**السبب:**
- الـ username في الـ token مختلف عن الـ username في قاعدة البيانات

**الحل:**
- احصل على token جديد

---

### **2️⃣ SecurityConfig** ⚠️⚠️

**المسار:** `src/main/java/com/example/chat_app/config/SecurityConfig.java`

#### **المشكلة:**

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
    .requestMatchers("/api/auth/**").permitAll()
    
    .anyRequest().authenticated()  // ← كل endpoint آخر يحتاج authentication
)
```

**متى يحدث 403:**
- أي endpoint **غير** `/api/auth/**`
- إذا لم يكن هناك authentication في `SecurityContext`

**الـ Endpoints المفتوحة (لا تحتاج token):**
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/**`

**الـ Endpoints المحمية (تحتاج token):**
- كل شيء آخر: `/api/messages/**`, `/api/chats/**`, `/api/contacts/**`, إلخ.

**الحل:**
- تأكد من إرسال valid token مع الـ requests للـ endpoints المحمية

---

### **3️⃣ WebSocketAuthInterceptor** ⚠️

**المسار:** `src/main/java/com/example/chat_app/config/WebSocketAuthInterceptor.java`

**يؤثر فقط على:** WebSocket connections (`/ws`)

#### **الأخطاء المحتملة:**

##### **أ) Token غير موجود في WebSocket connection**
```
Error: Missing Authorization header for WebSocket connection
```

**الحل:**
```javascript
// في الـ frontend، أضف الـ token في الـ headers:
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: `Bearer ${token}` },  // ← هنا
  (frame) => {
    console.log('Connected: ' + frame);
  }
);
```

##### **ب) Token غير صحيح في WebSocket**
```
Error: Invalid JWT token for WebSocket connection
```

**الحل:**
- استخدم token صحيح وغير منتهي الصلاحية

---

## 🔍 **Service Layer Authorization** (ليست middleware)

هذه **ليست middleware** لكنها تسبب 403 أيضاً!

### **MessageService.java**

#### **1. User ليس عضو في الـ Chat**
```java
throw new AppException("User is not a member of this chat.", HttpStatus.FORBIDDEN);
```

**متى يحدث:**
- عند محاولة إرسال رسالة لـ chat أنت لست عضو فيه

**الحل:**
- تأكد من أنك عضو في الـ chat قبل إرسال الرسالة

---

#### **2. User لا يمكنه عرض الرسائل**
```java
throw new AppException("User is not a member of this chat and cannot view messages.", HttpStatus.FORBIDDEN);
```

**متى يحدث:**
- عند محاولة عرض رسائل chat أنت لست عضو فيه

**الحل:**
- تأكد من أنك عضو في الـ chat

---

#### **3. User غير مصرح له بتعديل الرسالة**
```java
throw new AppException("User is not authorized to edit this message.", HttpStatus.FORBIDDEN);
```

**متى يحدث:**
- عند محاولة تعديل رسالة لم ترسلها أنت

**الحل:**
- يمكنك فقط تعديل الرسائل التي أرسلتها أنت

---

#### **4. User غير مصرح له بحذف الرسالة**
```java
throw new AppException("User is not authorized to delete this message.", HttpStatus.FORBIDDEN);
```

**متى يحدث:**
- عند محاولة حذف رسالة لم ترسلها أنت

**الحل:**
- يمكنك فقط حذف الرسائل التي أرسلتها أنت

---

### **ContactService.java**

#### **1. محاولة تعديل contact لا تملكه**
```java
HttpStatus.FORBIDDEN
```

**متى يحدث:**
- عند محاولة تعديل contact لمستخدم آخر

**الحل:**
- يمكنك فقط تعديل contacts الخاصة بك

---

#### **2. محاولة حذف contact لا تملكه**
```java
HttpStatus.FORBIDDEN
```

**متى يحدث:**
- عند محاولة حذف contact لمستخدم آخر

**الحل:**
- يمكنك فقط حذف contacts الخاصة بك

---

### **ChatService.java**

#### **1. User غير مصرح له بعرض أعضاء الـ Chat**
```java
throw new AppException("User is not authorized to view the members of this chat.", HttpStatus.FORBIDDEN);
```

**متى يحدث:**
- عند محاولة عرض أعضاء chat أنت لست عضو فيه

**الحل:**
- تأكد من أنك عضو في الـ chat

---

#### **2. User ليس عضو في الـ Chat**
```java
throw new AppException("User is not a member of the chat", HttpStatus.FORBIDDEN);
```

**متى يحدث:**
- عند محاولة تنفيذ عملية على chat أنت لست عضو فيه

**الحل:**
- تأكد من أنك عضو في الـ chat

---

#### **3. Target User ليس عضو في الـ Chat**
```java
throw new AppException("Target user is not a member of this chat.", HttpStatus.FORBIDDEN);
```

**متى يحدث:**
- عند محاولة تعديل role لمستخدم ليس عضو في الـ chat

**الحل:**
- تأكد من أن المستخدم المستهدف عضو في الـ chat

---

#### **4. محاولة تعديل role لـ Admin**
```java
throw new AppException("Cannot modify the role of an existing group ADMIN.", HttpStatus.FORBIDDEN);
```

**متى يحدث:**
- عند محاولة تغيير role لـ admin موجود

**الحل:**
- لا يمكن تعديل role الـ admin

---

## 📊 **خطوات التشخيص السريع**

### **1. افحص الـ Logs**

بعد التحديث الأخير، الـ `JwtAuthenticationFilter` سيطبع logs واضحة:

```
✅ JWT Auth Success: username for path: /api/...
❌ JWT Token Expired for path: /api/...
❌ Malformed JWT Token for path: /api/...
❌ Invalid JWT Signature (wrong secret key?) for path: /api/...
❌ JWT Invalid (expired or username mismatch) for: ...
=== No Bearer Token found for: /api/...
```

**ابحث في الـ console عن هذه الرسائل لتحديد المشكلة بالضبط**

---

### **2. افحص الـ Token**

استخدم [jwt.io](https://jwt.io) لفك تشفير الـ token:

```json
{
  "sub": "username",      // ← تأكد من أن الـ username صحيح
  "iat": 1640000000,      // ← وقت الإنشاء
  "exp": 1640086400       // ← وقت الانتهاء (تأكد أنه لم ينتهي)
}
```

**تحقق من:**
- ✅ `exp` (expiration) لم ينتهي بعد
- ✅ `sub` (username) موجود في قاعدة البيانات
- ✅ الـ signature صحيح (باستخدام نفس الـ secret key)

---

### **3. افحص الـ Request**

#### **في Postman:**
```
Headers:
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### **في JavaScript:**
```javascript
fetch('/api/messages', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
```

---

### **4. افحص الـ Endpoint**

**الـ Endpoints المفتوحة (لا تحتاج token):**
- ✅ `POST /api/auth/register`
- ✅ `POST /api/auth/login`
- ✅ `GET /api/auth/**`

**الـ Endpoints المحمية (تحتاج token):**
- 🔒 كل شيء آخر

---

## 🛠️ **الحلول السريعة**

### **الحل 1: احصل على Token جديد**
```bash
POST /api/auth/login
{
  "username": "your-username",
  "password": "your-password"
}

# Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### **الحل 2: تأكد من الـ Authorization Header**
```http
Authorization: Bearer <your-token-here>
```

---

### **الحل 3: افحص الـ Logs**
```
=== Incoming Request: GET /api/messages
✅ JWT Auth Success: username for path: /api/messages
```

أو:
```
=== Incoming Request: GET /api/messages
❌ JWT Token Expired for path: /api/messages | Error: ...
```

---

### **الحل 4: زيادة مدة صلاحية الـ Token**

في `application.properties`:
```properties
# 24 hours (current)
application.security.jwt.expiration=86400000

# 7 days
application.security.jwt.expiration=604800000

# 30 days
application.security.jwt.expiration=2592000000
```

---

## 📞 **الخلاصة**

### **الأسباب الرئيسية لـ Access Denied:**

1. ⏰ **Token منتهي الصلاحية** - احصل على token جديد
2. 🚫 **Token غير موجود** - أضف `Authorization: Bearer <token>`
3. 🔨 **Token معطوب** - تأكد من نسخ الـ token كاملاً
4. 🔑 **Secret Key خاطئ** - تأكد من الـ secret key في `application.properties`
5. 👤 **User غير موجود** - تأكد من أن المستخدم موجود
6. 🔒 **Endpoint محمي** - تأكد من إرسال token صحيح
7. 🚷 **Authorization في Business Logic** - تأكد من أنك مصرح لك بالعملية

---

## 🎯 **الخطوات الموصى بها:**

1. ✅ افحص الـ **Logs** في الـ console
2. ✅ افحص الـ **Token** على jwt.io
3. ✅ تأكد من الـ **Authorization Header**
4. ✅ احصل على **Token جديد** إذا لزم الأمر
5. ✅ تأكد من أنك **مصرح لك** بالعملية المطلوبة
poiuytrewq  88855xz
---

**تم إنشاء هذا الدليل في:** 2025-12-22
