# Chat Application - Complete API Documentation

**Version:** 1.0.0  
**Base URL:** `http://localhost:8080`  
**Authentication:** JWT Bearer Token  
**Content-Type:** `application/json`

---

# SECTION A — API SUMMARY TABLE

| Feature/Module | Method | Endpoint                               | Auth | Roles        | Request DTO / Body             | Response DTO                   | Notes                    |
| -------------- | ------ | -------------------------------------- | ---- | ------------ | ------------------------------ | ------------------------------ | ------------------------ |
| **Auth**       | POST   | `/api/auth/register`                   | No   | -            | `RegisterRequest`              | `AuthenticationResponse`       | User registration        |
| **Auth**       | POST   | `/api/auth/login`                      | No   | -            | `LoginRequest`                 | `AuthenticationResponse`       | User login               |
| **Users**      | GET    | `/api/users/profile`                   | Yes  | USER         | -                              | `UserResponse`                 | Get current user profile |
| **Users**      | GET    | `/api/users/all`                       | Yes  | USER         | -                              | `List<UserResponse>`           | Get all users            |
| **Chats**      | GET    | `/api/chats`                           | Yes  | USER         | -                              | `List<ChatDisplayDto>`         | Get user's chats         |
| **Chats**      | POST   | `/api/chats`                           | Yes  | USER         | `CreateChatRequest`            | `String`                       | Create P2P/Group chat    |
| **Chats**      | PATCH  | `/api/chats/properties`                | Yes  | USER (ADMIN) | `UpdateGroupPropertiesRequest` | `String`                       | Update group name/image  |
| **Chats**      | GET    | `/api/chats/members/{chatId}`          | Yes  | USER         | -                              | `List<MemberDisplayDto>`       | Get chat members         |
| **Chats**      | POST   | `/api/chats/members`                   | Yes  | USER (ADMIN) | `UpdateMembershipRequest`      | `String`                       | Add members to group     |
| **Chats**      | DELETE | `/api/chats/members`                   | Yes  | USER (ADMIN) | `UpdateMembershipRequest`      | `204 No Content`               | Remove members           |
| **Chats**      | PATCH  | `/api/chats/roles`                     | Yes  | USER (ADMIN) | `UpdateMemberRoleRequest`      | `String`                       | Update member role       |
| **Contacts**   | GET    | `/api/contacts`                        | Yes  | USER         | -                              | `List<ContactDisplayResponse>` | Get all contacts         |
| **Contacts**   | GET    | `/api/contacts/phone`                  | Yes  | USER         | `?phone=`                      | `ContactMatchResponse`         | Find user by phone       |
| **Contacts**   | POST   | `/api/contacts/sync`                   | Yes  | USER         | `SyncContactRequest`           | `List<ContactMatchResponse>`   | Sync phone contacts      |
| **Contacts**   | POST   | `/api/contacts/add`                    | Yes  | USER         | `AddContactRequest`            | `String`                       | Add new contact          |
| **Contacts**   | PATCH  | `/api/contacts/update`                 | Yes  | USER         | `UpdateContactRequest`         | `String`                       | Update contact           |
| **Contacts**   | DELETE | `/api/contacts/delete/{contactUserId}` | Yes  | USER         | -                              | `204 No Content`               | Delete contact           |
| **Messages**   | POST   | `/api/messages`                        | Yes  | USER         | `SendMessageRequest`           | `String`                       | Send message             |
| **Messages**   | GET    | `/api/messages/{chatId}`               | Yes  | USER         | `?page=&size=`                 | `Page<MessageDisplayDto>`      | Get chat messages        |
| **Messages**   | PATCH  | `/api/messages`                        | Yes  | USER         | `UpdateMessageRequest`         | `String`                       | Edit message             |
| **Messages**   | DELETE | `/api/messages/{messageId}`            | Yes  | USER         | -                              | `204 No Content`               | Delete message           |

---

# SECTION B — FULL ENDPOINT DETAILS

---

## Authentication Module

### Auth — Register User

- **Method:** `POST`
- **URL:** `/api/auth/register`
- **Auth:** Not Required
- **Roles:** None (Public endpoint)
- **Headers:**
  - `Content-Type: application/json`
- **Request Body:**

```java
public record RegisterRequest(
    String name,           // User's display name
    String username,       // Unique username
    String phoneNumber,    // Phone number
    String password        // Password
) {}
```

**Example:**

```json
{
  "name": "John Doe",
  "username": "johndoe",
  "phoneNumber": "+1234567890",
  "password": "securePassword123"
}
```

- **Success Response:** `200 OK`

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

- **Error Responses:**
  - `500 Internal Server Error` - Username already taken

```json
{
  "timestamp": "2025-12-19T20:37:37",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred."
}
```

- **Notes:** JWT token expires in 24 hours (86400000ms)

---

### Auth — Login User

- **Method:** `POST`
- **URL:** `/api/auth/login`
- **Auth:** Not Required
- **Roles:** None (Public endpoint)
- **Headers:**
  - `Content-Type: application/json`
- **Request Body:**

```java
public record LoginRequest(
    String username,    // User's username
    String password     // User's password
) {}
```

**Example:**

```json
{
  "username": "johndoe",
  "password": "securePassword123"
}
```

- **Success Response:** `200 OK`

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

- **Error Responses:**
  - `500 Internal Server Error` - Invalid credentials
- **Notes:** Uses JWT authentication strategy

---

## Users Module

### Users — Get Current User Profile

- **Method:** `GET`
- **URL:** `/api/users/profile`
- **Auth:** Required
- **Roles:** Any authenticated user (ROLE_USER)
- **Headers:**
  - `Authorization: Bearer <token>`
- **Success Response:** `200 OK`

```java
public record UserResponse(
    UUID ID,
    String name,
    String username,
    String phoneNumber
) {}
```

**Example:**

```json
{
  "ID": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "username": "johndoe",
  "phoneNumber": "+1234567890"
}
```

- **Error Responses:**
  - `401 Unauthorized` - Missing or invalid token
  - `500 Internal Server Error` - User not found in DB

---

### Users — Get All Users

- **Method:** `GET`
- **URL:** `/api/users/all`
- **Auth:** Required
- **Roles:** Any authenticated user (ROLE_USER)
- **Headers:**
  - `Authorization: Bearer <token>`
- **Success Response:** `200 OK`

```json
[
  {
    "ID": "550e8400-e29b-41d4-a716-446655440000",
    "name": "John Doe",
    "username": "johndoe",
    "phoneNumber": "+1234567890"
  }
]
```

- **Error Responses:**
  - `401 Unauthorized` - Missing or invalid token

---

## Chats Module

### Chats — Get User's Chats

- **Method:** `GET`
- **URL:** `/api/chats`
- **Auth:** Required
- **Roles:** Any authenticated user
- **Headers:**
  - `Authorization: Bearer <token>`
- **Success Response:** `200 OK`

```java
public record ChatDisplayDto(
    UUID chatId,
    String chatType,           // "P2P" or "GROUP"
    String groupName,          // null for P2P
    String groupImage,         // null for P2P
    List<MemberDisplayDto> members
) {}

public record MemberDisplayDto(
    UUID userId,
    String username,
    String role                // "ADMIN" or "MEMBER"
) {}
```

**Example:**

```json
[
  {
    "chatId": "550e8400-e29b-41d4-a716-446655440001",
    "chatType": "GROUP",
    "groupName": "Family Chat",
    "groupImage": "https://example.com/image.jpg",
    "members": [
      {
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "username": "johndoe",
        "role": "ADMIN"
      }
    ]
  }
]
```

---

### Chats — Create Chat

- **Method:** `POST`
- **URL:** `/api/chats`
- **Auth:** Required
- **Roles:** Any authenticated user
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Request Body:**

```java
public record CreateChatRequest(
    String chatType,           // "P2P" or "GROUP"
    String groupName,          // Required for GROUP
    String groupImage,         // Optional
    List<UUID> membersId       // User IDs to add
) {}
```

**Example (P2P):**

```json
{
  "chatType": "P2P",
  "groupName": null,
  "groupImage": null,
  "membersId": ["550e8400-e29b-41d4-a716-446655440002"]
}
```

**Example (GROUP):**

```json
{
  "chatType": "GROUP",
  "groupName": "Family Chat",
  "groupImage": "https://example.com/image.jpg",
  "membersId": ["550e8400-e29b-41d4-a716-446655440002", "550e8400-e29b-41d4-a716-446655440003"]
}
```

- **Success Response:** `200 OK`

```json
"Chat created successfully."
```

- **Error Responses:**
  - `400 Bad Request`:
    - "Some users were not found"
    - "P2P chat must have exactly 2 users"
    - "Group name is required"
    - "Group chat must have at least 3 users"
    - "Invalid chat type"
- **WebSocket Notification:** `/user/{username}/queue/newChat` → `ChatDisplayDto`

---

### Chats — Get Chat Members

- **Method:** `GET`
- **URL:** `/api/chats/members/{chatId}`
- **Auth:** Required
- **Roles:** Chat member only
- **Headers:**
  - `Authorization: Bearer <token>`
- **Path Params:**
  - `chatId`: UUID (required) — Chat identifier
- **Success Response:** `200 OK`

```json
[
  {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "johndoe",
    "role": "ADMIN"
  }
]
```

- **Error Responses:**
  - `403 Forbidden` - "User is not authorized to view the members of this chat."

---

### Chats — Update Group Properties

- **Method:** `PATCH`
- **URL:** `/api/chats/properties`
- **Auth:** Required
- **Roles:** Group ADMIN only
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Request Body:**

```java
public record UpdateGroupPropertiesRequest(
    UUID chatId,
    String newGroupName,       // Optional
    String newGroupImageUrl    // Optional
) {}
```

- **Success Response:** `200 OK`

```json
"Group properties updated successfully."
```

- **Error Responses:**
  - `403 Forbidden` - Not a member or not ADMIN
  - `404 Not Found` - Chat not found
- **WebSocket Notification:** `/topic/chat/{chatId}/updates` → `ChatDisplayDto`

---

### Chats — Add Members

- **Method:** `POST`
- **URL:** `/api/chats/members`
- **Auth:** Required
- **Roles:** Group ADMIN only
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Request Body:**

```java
public record UpdateMembershipRequest(
    UUID chatId,
    List<UUID> memberUserIds
) {}
```

- **Success Response:** `200 OK`

```json
"Members added successfully."
```

- **Error Responses:**
  - `400 Bad Request` - "One or more user IDs to add are invalid."
  - `403 Forbidden` - Not ADMIN
  - `404 Not Found` - Chat not found
- **WebSocket Notification:** `/topic/chat/{chatId}/members` → `MemberUpdateDto`

---

### Chats — Remove Members

- **Method:** `DELETE`
- **URL:** `/api/chats/members`
- **Auth:** Required
- **Roles:** Group ADMIN or self-removal
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Request Body:**

```java
public record UpdateMembershipRequest(
    UUID chatId,
    List<UUID> memberUserIds
) {}
```

- **Success Response:** `204 No Content`
- **Error Responses:**
  - `403 Forbidden`:
    - "Group owner cannot remove themselves using this function."
    - "User does not have permission to remove one of the specified members."
  - `404 Not Found` - "No matching members found to remove from the chat."
- **WebSocket Notifications:**
  - `/user/{username}/queue/chatRemoved` → `UUID` (chatId)
  - `/topic/chat/{chatId}/members` → `MemberUpdateDto`

---

### Chats — Update Member Role

- **Method:** `PATCH`
- **URL:** `/api/chats/roles`
- **Auth:** Required
- **Roles:** Group ADMIN only
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Request Body:**

```java
public record UpdateMemberRoleRequest(
    UUID chatId,
    UUID targetUserId,
    String newRole             // "ADMIN" or "MEMBER"
) {}
```

- **Success Response:** `200 OK`

```json
"Member role updated successfully."
```

- **Error Responses:**
  - `400 Bad Request` - "Invalid role provided. Role must be ADMIN or MEMBER."
  - `403 Forbidden`:
    - Not a member or not ADMIN
    - "Cannot modify the role of an existing group ADMIN."
- **WebSocket Notification:** `/topic/chat/{chatId}/members` → `MemberUpdateDto`

---

## Contacts Module

### Contacts — Get All Contacts

- **Method:** `GET`
- **URL:** `/api/contacts`
- **Auth:** Required
- **Roles:** Any authenticated user
- **Headers:**
  - `Authorization: Bearer <token>`
- **Success Response:** `200 OK`

```java
public record ContactDisplayResponse(
    UUID ID,                    // Contact relationship ID
    UUID contactUserId,         // The contact user's ID
    String displayName,         // Custom saved name
    String contactUsername,
    String contactPhoneNumber
) {}
```

**Example:**

```json
[
  {
    "ID": "550e8400-e29b-41d4-a716-446655440010",
    "contactUserId": "550e8400-e29b-41d4-a716-446655440002",
    "displayName": "Mom",
    "contactUsername": "janedoe",
    "contactPhoneNumber": "+1987654321"
  }
]
```

---

### Contacts — Find by Phone

- **Method:** `GET`
- **URL:** `/api/contacts/phone`
- **Auth:** Required
- **Roles:** Any authenticated user
- **Headers:**
  - `Authorization: Bearer <token>`
- **Query Params:**
  - `phone`: String (required) — Phone number to search
- **Success Response:** `200 OK`

```java
public record ContactMatchResponse(
    UUID id,
    String userName,
    String name,
    String phoneNumber
) {}
```

- **Error Responses:**
  - `404 Not Found` - No user with that phone number

---

### Contacts — Sync Contacts

- **Method:** `POST`
- **URL:** `/api/contacts/sync`
- **Auth:** Required
- **Roles:** Any authenticated user
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Request Body:**

```java
public record SyncContactRequest(
    List<String> phoneNumbers
) {}
```

**Example:**

```json
{
  "phoneNumbers": ["+1234567890", "+1987654321", "+1555555555"]
}
```

- **Success Response:** `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440002",
    "userName": "janedoe",
    "name": "Jane Doe",
    "phoneNumber": "+1987654321"
  }
]
```

---

### Contacts — Add Contact

- **Method:** `POST`
- **URL:** `/api/contacts/add`
- **Auth:** Required
- **Roles:** Any authenticated user
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Request Body:**

```java
public record AddContactRequest(
    String targetPhoneNumber,
    String customDisplayName
) {}
```

- **Success Response:** `200 OK`

```json
"Contact added successfully"
```

- **Error Responses:**
  - `400 Bad Request`:
    - "Cannot add yourself as a contact"
    - "Contact already added"
  - `404 Not Found` - "Target user not found"
- **WebSocket Notification:** `/user/{targetUsername}/queue/contactUpdates` → `ContactNotificationDto`

---

### Contacts — Update Contact

- **Method:** `PATCH`
- **URL:** `/api/contacts/update`
- **Auth:** Required
- **Roles:** Any authenticated user
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Request Body:**

```java
public record UpdateContactRequest(
    UUID targetUserId,
    String newDisplayName,     // Optional
    String newPhoneNumber      // Optional
) {}
```

- **Success Response:** `200 OK`

```json
"Contact updated successfully"
```

- **Error Responses:**
  - `403 Forbidden` - "Contact relationship not found or unauthorized"
  - `404 Not Found` - "Target user not found"

---

### Contacts — Delete Contact

- **Method:** `DELETE`
- **URL:** `/api/contacts/delete/{contactUserId}`
- **Auth:** Required
- **Roles:** Any authenticated user
- **Headers:**
  - `Authorization: Bearer <token>`
- **Path Params:**
  - `contactUserId`: UUID (required)
- **Success Response:** `204 No Content`
- **Error Responses:**
  - `403 Forbidden` - "Contact not found or unauthorized"
- **WebSocket Notification:** `/user/{targetUsername}/queue/contactUpdates` → `ContactNotificationDto`

---

## Messages Module

### Messages — Send Message

- **Method:** `POST`
- **URL:** `/api/messages`
- **Auth:** Required
- **Roles:** Chat member only
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Request Body:**

```java
public record SendMessageRequest(
    UUID chatId,
    String messageType,        // "TEXT", "IMAGE", "AUDIO", "VIDEO"
    String content,
    String mediaUrl            // For media messages
) {}
```

**Example:**

```json
{
  "chatId": "550e8400-e29b-41d4-a716-446655440001",
  "messageType": "TEXT",
  "content": "Hello everyone!",
  "mediaUrl": null
}
```

- **Success Response:** `200 OK`

```json
"Message sent successfully."
```

- **Error Responses:**
  - `404 Not Found` - "Chat not found."
- **WebSocket Notification:** `/topic/chat/{chatId}` → `MessageDisplayDto`

---

### Messages — Get Chat Messages

- **Method:** `GET`
- **URL:** `/api/messages/{chatId}`
- **Auth:** Required
- **Roles:** Chat member only
- **Headers:**
  - `Authorization: Bearer <token>`
- **Path Params:**
  - `chatId`: UUID (required)
- **Query Params:**
  - `page`: int (optional, default=0)
  - `size`: int (optional, default=20)
- **Success Response:** `200 OK`

```java
public record MessageDisplayDto(
    UUID messageId,
    SenderDto sender,
    String messageType,
    String content,
    String mediaUrl,
    LocalDateTime timestamp,
    boolean isEdited,
    boolean isDeleted
) {}

public record SenderDto(
    UUID senderId,
    String username
) {}
```

**Example (Spring Page wrapper):**

```json
{
  "content": [
    {
      "messageId": "550e8400-e29b-41d4-a716-446655440020",
      "sender": {
        "senderId": "550e8400-e29b-41d4-a716-446655440000",
        "username": "johndoe"
      },
      "messageType": "TEXT",
      "content": "Hello!",
      "mediaUrl": null,
      "timestamp": "2025-12-19T20:30:00",
      "isEdited": false,
      "isDeleted": false
    }
  ],
  "pageable": { ... },
  "totalElements": 50,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

- **Error Responses:**
  - `403 Forbidden` - "User is not a member of this chat and cannot view messages."
- **Notes:** Messages sorted by `sentAt` descending (newest first)

---

### Messages — Edit Message

- **Method:** `PATCH`
- **URL:** `/api/messages`
- **Auth:** Required
- **Roles:** Message sender only
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Request Body:**

```java
public record UpdateMessageRequest(
    UUID messageId,
    String newContent
) {}
```

- **Success Response:** `200 OK`

```json
"Message updated successfully."
```

- **Error Responses:**
  - `400 Bad Request`:
    - "Only text messages can be edited."
    - "New message content cannot be empty."
  - `401 Unauthorized` - "User is not authorized to edit this message."
  - `404 Not Found` - "Message not found."
- **WebSocket Notification:** `/topic/chat/{chatId}` → `MessageDisplayDto`

---

### Messages — Delete Message

- **Method:** `DELETE`
- **URL:** `/api/messages/{messageId}`
- **Auth:** Required
- **Roles:** Message sender OR Group ADMIN
- **Headers:**
  - `Authorization: Bearer <token>`
- **Path Params:**
  - `messageId`: UUID (required)
- **Success Response:** `204 No Content`
- **Error Responses:**
  - `401 Unauthorized` - "User is not authorized to delete this message."
  - `404 Not Found` - "Message not found."
- **Notes:** Soft delete - sets `isDeleted=true`, content shows "Message has been deleted"
- **WebSocket Notification:** `/topic/chat/{chatId}` → `MessageDisplayDto`

---

## WebSocket Endpoints

### Connection

- **URL:** `ws://localhost:8080/ws` (with SockJS fallback)
- **Allowed Origins:** `*`

### Subscribe Destinations

| Destination                             | Payload                  | Description           |
| --------------------------------------- | ------------------------ | --------------------- |
| `/user/{username}/queue/newChat`        | `ChatDisplayDto`         | New chat created      |
| `/user/{username}/queue/chatRemoved`    | `UUID`                   | Removed from chat     |
| `/user/{username}/queue/contactUpdates` | `ContactNotificationDto` | Contact events        |
| `/topic/chat/{chatId}`                  | `MessageDisplayDto`      | New/updated messages  |
| `/topic/chat/{chatId}/members`          | `MemberUpdateDto`        | Member changes        |
| `/topic/chat/{chatId}/updates`          | `ChatDisplayDto`         | Chat property updates |

### Application Destination Prefix

- `/app` - For sending messages to server

---

## Error Response Format

All errors follow this structure:

```json
{
  "timestamp": "2025-12-19T20:37:37",
  "status": 400,
  "error": "Bad Request",
  "message": "Descriptive error message"
}
```

---

## Enums Reference

```java
public enum Enums {
    // Call status
    COMPLETED, MISSED, CANCELED, FAILED,
    // Chat types
    P2P, GROUP,
    // Call types
    VIDEO, VOICE,
    // Message types
    TEXT, IMAGE, AUDIO,
    // Member roles
    ADMIN, MEMBER
}
```
