# SECTION C — JavaFX Integration & State Management Plan

## 1. Architecture Overview

### Package Structure

```
com.example.chatapp.client/
├── ChatApplication.java              # Main JavaFX Application
├── api/
│   ├── ApiClient.java               # HTTP Client wrapper
│   ├── ApiException.java            # Custom exception
│   ├── TokenStorage.java            # JWT token management
│   └── endpoints/
│       ├── AuthApi.java             # Authentication endpoints
│       ├── UsersApi.java            # User endpoints
│       ├── ChatsApi.java            # Chat endpoints
│       ├── ContactsApi.java         # Contacts endpoints
│       └── MessagesApi.java         # Messages endpoints
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── CreateChatRequest.java
│   │   ├── SendMessageRequest.java
│   │   ├── UpdateMessageRequest.java
│   │   ├── AddContactRequest.java
│   │   ├── UpdateContactRequest.java
│   │   ├── SyncContactRequest.java
│   │   ├── UpdateGroupPropertiesRequest.java
│   │   ├── UpdateMembershipRequest.java
│   │   └── UpdateMemberRoleRequest.java
│   └── response/
│       ├── AuthenticationResponse.java
│       ├── UserResponse.java
│       ├── ChatDisplayDto.java
│       ├── MemberDisplayDto.java
│       ├── MessageDisplayDto.java
│       ├── SenderDto.java
│       ├── ContactDisplayResponse.java
│       ├── ContactMatchResponse.java
│       ├── ContactNotificationDto.java
│       ├── MemberUpdateDto.java
│       └── PageResponse.java         # Generic page wrapper
├── store/
│   ├── AuthStore.java               # Auth state
│   ├── ChatStore.java               # Chats state
│   ├── ContactStore.java            # Contacts state
│   └── MessageStore.java            # Messages state
├── service/
│   ├── AuthService.java             # Auth business logic
│   ├── ChatService.java             # Chat business logic
│   ├── ContactService.java          # Contacts business logic
│   ├── MessageService.java          # Messages business logic
│   └── WebSocketService.java        # WebSocket handling
├── ui/
│   ├── controller/
│   │   ├── LoginController.java
│   │   ├── RegisterController.java
│   │   ├── HomeController.java
│   │   ├── ChatController.java
│   │   ├── ContactsController.java
│   │   ├── GroupController.java
│   │   └── SettingsController.java
│   └── fxml/
│       ├── Login.fxml
│       ├── Register.fxml
│       ├── Home.fxml
│       ├── Chat.fxml
│       ├── Contacts.fxml
│       ├── Group.fxml
│       └── Settings.fxml
└── util/
    ├── JsonMapper.java              # Jackson ObjectMapper config
    ├── NavigationManager.java       # Scene navigation
    └── AlertUtils.java              # Alert dialogs
```

---

## 2. Core Infrastructure Code

### 2.1 ApiClient.java - Central HTTP Client

```java
package com.example.chatapp.client.api;

import com.example.chatapp.client.util.JsonMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient httpClient;
    private final TokenStorage tokenStorage;

    public ApiClient(TokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    // GET request
    public <T> CompletableFuture<T> get(String path, TypeReference<T> typeRef) {
        HttpRequest request = buildRequest(path)
            .GET()
            .build();
        return sendAsync(request, typeRef);
    }

    // POST request
    public <T> CompletableFuture<T> post(String path, Object body, TypeReference<T> typeRef) {
        HttpRequest request = buildRequest(path)
            .POST(HttpRequest.BodyPublishers.ofString(JsonMapper.toJson(body)))
            .build();
        return sendAsync(request, typeRef);
    }

    // PATCH request
    public <T> CompletableFuture<T> patch(String path, Object body, TypeReference<T> typeRef) {
        HttpRequest request = buildRequest(path)
            .method("PATCH", HttpRequest.BodyPublishers.ofString(JsonMapper.toJson(body)))
            .build();
        return sendAsync(request, typeRef);
    }

    // DELETE request
    public CompletableFuture<Void> delete(String path) {
        HttpRequest request = buildRequest(path)
            .DELETE()
            .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .thenApply(response -> {
                handleErrorStatus(response.statusCode(), "");
                return null;
            });
    }

    // DELETE with body
    public CompletableFuture<Void> deleteWithBody(String path, Object body) {
        HttpRequest request = buildRequest(path)
            .method("DELETE", HttpRequest.BodyPublishers.ofString(JsonMapper.toJson(body)))
            .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .thenApply(response -> {
                handleErrorStatus(response.statusCode(), "");
                return null;
            });
    }

    private HttpRequest.Builder buildRequest(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(30));

        String token = tokenStorage.getToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return builder;
    }

    private <T> CompletableFuture<T> sendAsync(HttpRequest request, TypeReference<T> typeRef) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                handleErrorStatus(response.statusCode(), response.body());
                if (typeRef.getType().equals(String.class)) {
                    return (T) response.body();
                }
                return JsonMapper.fromJson(response.body(), typeRef);
            });
    }

    private void handleErrorStatus(int statusCode, String body) {
        if (statusCode >= 400) {
            String message = extractErrorMessage(body);
            throw new ApiException(statusCode, message);
        }
    }

    private String extractErrorMessage(String body) {
        try {
            var errorMap = JsonMapper.fromJson(body,
                new TypeReference<java.util.Map<String, Object>>() {});
            return (String) errorMap.getOrDefault("message", "Unknown error");
        } catch (Exception e) {
            return body.isEmpty() ? "Unknown error" : body;
        }
    }
}
```

### 2.2 TokenStorage.java

```java
package com.example.chatapp.client.api;

import java.io.*;
import java.nio.file.*;
import java.util.prefs.Preferences;

public class TokenStorage {

    private static final String TOKEN_KEY = "jwt_token";
    private static final String TOKEN_FILE = "chat_token.dat";

    private String token;
    private final Preferences prefs;
    private final Path tokenFilePath;

    public TokenStorage() {
        this.prefs = Preferences.userNodeForPackage(TokenStorage.class);
        this.tokenFilePath = Paths.get(System.getProperty("user.home"), ".chatapp", TOKEN_FILE);
        loadToken();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        saveToken();
    }

    public void clearToken() {
        this.token = null;
        prefs.remove(TOKEN_KEY);
        try {
            Files.deleteIfExists(tokenFilePath);
        } catch (IOException ignored) {}
    }

    public boolean hasToken() {
        return token != null && !token.isEmpty();
    }

    private void loadToken() {
        // Try preferences first
        this.token = prefs.get(TOKEN_KEY, null);

        // Fallback to file
        if (token == null && Files.exists(tokenFilePath)) {
            try {
                this.token = Files.readString(tokenFilePath).trim();
            } catch (IOException ignored) {}
        }
    }

    private void saveToken() {
        if (token != null) {
            prefs.put(TOKEN_KEY, token);
            try {
                Files.createDirectories(tokenFilePath.getParent());
                Files.writeString(tokenFilePath, token);
            } catch (IOException ignored) {}
        }
    }
}
```

### 2.3 ApiException.java

```java
package com.example.chatapp.client.api;

public class ApiException extends RuntimeException {

    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isUnauthorized() {
        return statusCode == 401;
    }

    public boolean isForbidden() {
        return statusCode == 403;
    }

    public boolean isNotFound() {
        return statusCode == 404;
    }

    public boolean isBadRequest() {
        return statusCode == 400;
    }
}
```

### 2.4 JsonMapper.java

```java
package com.example.chatapp.client.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonMapper {

    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return mapper.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }
}
```

---

## 3. API Endpoint Classes

### 3.1 AuthApi.java

```java
package com.example.chatapp.client.api.endpoints;

import com.example.chatapp.client.api.ApiClient;
import com.example.chatapp.client.dto.request.LoginRequest;
import com.example.chatapp.client.dto.request.RegisterRequest;
import com.example.chatapp.client.dto.response.AuthenticationResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.concurrent.CompletableFuture;

public class AuthApi {

    private final ApiClient client;

    public AuthApi(ApiClient client) {
        this.client = client;
    }

    public CompletableFuture<AuthenticationResponse> login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        return client.post("/api/auth/login", request,
            new TypeReference<AuthenticationResponse>() {});
    }

    public CompletableFuture<AuthenticationResponse> register(
            String name, String username, String phoneNumber, String password) {
        RegisterRequest request = new RegisterRequest(name, username, phoneNumber, password);
        return client.post("/api/auth/register", request,
            new TypeReference<AuthenticationResponse>() {});
    }
}
```

### 3.2 UsersApi.java

```java
package com.example.chatapp.client.api.endpoints;

import com.example.chatapp.client.api.ApiClient;
import com.example.chatapp.client.dto.response.UserResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UsersApi {

    private final ApiClient client;

    public UsersApi(ApiClient client) {
        this.client = client;
    }

    public CompletableFuture<UserResponse> getProfile() {
        return client.get("/api/users/profile", new TypeReference<>() {});
    }

    public CompletableFuture<List<UserResponse>> getAllUsers() {
        return client.get("/api/users/all", new TypeReference<>() {});
    }
}
```

### 3.3 ChatsApi.java

```java
package com.example.chatapp.client.api.endpoints;

import com.example.chatapp.client.api.ApiClient;
import com.example.chatapp.client.dto.request.*;
import com.example.chatapp.client.dto.response.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChatsApi {

    private final ApiClient client;

    public ChatsApi(ApiClient client) {
        this.client = client;
    }

    public CompletableFuture<List<ChatDisplayDto>> getUserChats() {
        return client.get("/api/chats", new TypeReference<>() {});
    }

    public CompletableFuture<String> createChat(CreateChatRequest request) {
        return client.post("/api/chats", request, new TypeReference<>() {});
    }

    public CompletableFuture<List<MemberDisplayDto>> getChatMembers(UUID chatId) {
        return client.get("/api/chats/members/" + chatId, new TypeReference<>() {});
    }

    public CompletableFuture<String> updateGroupProperties(UpdateGroupPropertiesRequest request) {
        return client.patch("/api/chats/properties", request, new TypeReference<>() {});
    }

    public CompletableFuture<String> addMembers(UpdateMembershipRequest request) {
        return client.post("/api/chats/members", request, new TypeReference<>() {});
    }

    public CompletableFuture<Void> removeMembers(UpdateMembershipRequest request) {
        return client.deleteWithBody("/api/chats/members", request);
    }

    public CompletableFuture<String> updateMemberRole(UpdateMemberRoleRequest request) {
        return client.patch("/api/chats/roles", request, new TypeReference<>() {});
    }
}
```

### 3.4 ContactsApi.java

```java
package com.example.chatapp.client.api.endpoints;

import com.example.chatapp.client.api.ApiClient;
import com.example.chatapp.client.dto.request.*;
import com.example.chatapp.client.dto.response.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ContactsApi {

    private final ApiClient client;

    public ContactsApi(ApiClient client) {
        this.client = client;
    }

    public CompletableFuture<List<ContactDisplayResponse>> getAllContacts() {
        return client.get("/api/contacts", new TypeReference<>() {});
    }

    public CompletableFuture<ContactMatchResponse> findByPhone(String phone) {
        return client.get("/api/contacts/phone?phone=" + phone, new TypeReference<>() {});
    }

    public CompletableFuture<List<ContactMatchResponse>> syncContacts(List<String> phoneNumbers) {
        SyncContactRequest request = new SyncContactRequest(phoneNumbers);
        return client.post("/api/contacts/sync", request, new TypeReference<>() {});
    }

    public CompletableFuture<String> addContact(String phoneNumber, String displayName) {
        AddContactRequest request = new AddContactRequest(phoneNumber, displayName);
        return client.post("/api/contacts/add", request, new TypeReference<>() {});
    }

    public CompletableFuture<String> updateContact(UpdateContactRequest request) {
        return client.patch("/api/contacts/update", request, new TypeReference<>() {});
    }

    public CompletableFuture<Void> deleteContact(UUID contactUserId) {
        return client.delete("/api/contacts/delete/" + contactUserId);
    }
}
```

### 3.5 MessagesApi.java

```java
package com.example.chatapp.client.api.endpoints;

import com.example.chatapp.client.api.ApiClient;
import com.example.chatapp.client.dto.request.*;
import com.example.chatapp.client.dto.response.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MessagesApi {

    private final ApiClient client;

    public MessagesApi(ApiClient client) {
        this.client = client;
    }

    public CompletableFuture<String> sendMessage(SendMessageRequest request) {
        return client.post("/api/messages", request, new TypeReference<>() {});
    }

    public CompletableFuture<PageResponse<MessageDisplayDto>> getChatMessages(
            UUID chatId, int page, int size) {
        String url = String.format("/api/messages/%s?page=%d&size=%d", chatId, page, size);
        return client.get(url, new TypeReference<>() {});
    }

    public CompletableFuture<String> editMessage(UUID messageId, String newContent) {
        UpdateMessageRequest request = new UpdateMessageRequest(messageId, newContent);
        return client.patch("/api/messages", request, new TypeReference<>() {});
    }

    public CompletableFuture<Void> deleteMessage(UUID messageId) {
        return client.delete("/api/messages/" + messageId);
    }
}
```

---

## 4. State Management (Stores)

### 4.1 AuthStore.java

```java
package com.example.chatapp.client.store;

import com.example.chatapp.client.api.TokenStorage;
import com.example.chatapp.client.dto.response.UserResponse;
import javafx.beans.property.*;

public class AuthStore {

    private final ObjectProperty<UserResponse> currentUser = new SimpleObjectProperty<>();
    private final BooleanProperty loggedIn = new SimpleBooleanProperty(false);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty error = new SimpleStringProperty();

    private final TokenStorage tokenStorage;

    public AuthStore(TokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
        this.loggedIn.set(tokenStorage.hasToken());
    }

    // Property getters
    public ObjectProperty<UserResponse> currentUserProperty() { return currentUser; }
    public BooleanProperty loggedInProperty() { return loggedIn; }
    public BooleanProperty loadingProperty() { return loading; }
    public StringProperty errorProperty() { return error; }

    // Value getters
    public UserResponse getCurrentUser() { return currentUser.get(); }
    public boolean isLoggedIn() { return loggedIn.get(); }
    public boolean isLoading() { return loading.get(); }
    public String getError() { return error.get(); }

    // Actions
    public void setCurrentUser(UserResponse user) {
        currentUser.set(user);
        loggedIn.set(user != null);
    }

    public void setToken(String token) {
        tokenStorage.setToken(token);
        loggedIn.set(true);
    }

    public void logout() {
        tokenStorage.clearToken();
        currentUser.set(null);
        loggedIn.set(false);
    }

    public void setLoading(boolean value) { loading.set(value); }
    public void setError(String value) { error.set(value); }
    public void clearError() { error.set(null); }
}
```

### 4.2 ChatStore.java

```java
package com.example.chatapp.client.store;

import com.example.chatapp.client.dto.response.*;
import javafx.beans.property.*;
import javafx.collections.*;
import java.util.UUID;

public class ChatStore {

    private final ObservableList<ChatDisplayDto> chats = FXCollections.observableArrayList();
    private final ObjectProperty<ChatDisplayDto> activeChat = new SimpleObjectProperty<>();
    private final ObservableList<MemberDisplayDto> activeChatMembers = FXCollections.observableArrayList();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty error = new SimpleStringProperty();

    // Property getters
    public ObservableList<ChatDisplayDto> getChats() { return chats; }
    public ObjectProperty<ChatDisplayDto> activeChatProperty() { return activeChat; }
    public ObservableList<MemberDisplayDto> getActiveChatMembers() { return activeChatMembers; }
    public BooleanProperty loadingProperty() { return loading; }
    public StringProperty errorProperty() { return error; }

    // Value getters
    public ChatDisplayDto getActiveChat() { return activeChat.get(); }
    public boolean isLoading() { return loading.get(); }

    // Actions
    public void setChats(java.util.List<ChatDisplayDto> chatList) {
        chats.setAll(chatList);
    }

    public void addChat(ChatDisplayDto chat) {
        // Add to beginning of list
        chats.add(0, chat);
    }

    public void setActiveChat(ChatDisplayDto chat) {
        activeChat.set(chat);
    }

    public void setActiveChatMembers(java.util.List<MemberDisplayDto> members) {
        activeChatMembers.setAll(members);
    }

    public void updateChat(ChatDisplayDto updatedChat) {
        for (int i = 0; i < chats.size(); i++) {
            if (chats.get(i).chatId().equals(updatedChat.chatId())) {
                chats.set(i, updatedChat);
                if (activeChat.get() != null &&
                    activeChat.get().chatId().equals(updatedChat.chatId())) {
                    activeChat.set(updatedChat);
                }
                break;
            }
        }
    }

    public void removeChat(UUID chatId) {
        chats.removeIf(chat -> chat.chatId().equals(chatId));
        if (activeChat.get() != null && activeChat.get().chatId().equals(chatId)) {
            activeChat.set(null);
        }
    }

    public void setLoading(boolean value) { loading.set(value); }
    public void setError(String value) { error.set(value); }
    public void clearError() { error.set(null); }
}
```

### 4.3 MessageStore.java

```java
package com.example.chatapp.client.store;

import com.example.chatapp.client.dto.response.MessageDisplayDto;
import javafx.beans.property.*;
import javafx.collections.*;
import java.util.UUID;

public class MessageStore {

    private final ObservableList<MessageDisplayDto> messages = FXCollections.observableArrayList();
    private final IntegerProperty currentPage = new SimpleIntegerProperty(0);
    private final IntegerProperty totalPages = new SimpleIntegerProperty(0);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty hasMore = new SimpleBooleanProperty(true);
    private final StringProperty error = new SimpleStringProperty();

    // Property getters
    public ObservableList<MessageDisplayDto> getMessages() { return messages; }
    public IntegerProperty currentPageProperty() { return currentPage; }
    public IntegerProperty totalPagesProperty() { return totalPages; }
    public BooleanProperty loadingProperty() { return loading; }
    public BooleanProperty hasMoreProperty() { return hasMore; }
    public StringProperty errorProperty() { return error; }

    // Actions
    public void setMessages(java.util.List<MessageDisplayDto> messageList, int page, int total) {
        if (page == 0) {
            messages.setAll(messageList);
        } else {
            messages.addAll(messageList);
        }
        currentPage.set(page);
        totalPages.set(total);
        hasMore.set(page < total - 1);
    }

    public void addMessage(MessageDisplayDto message) {
        // Add new message at beginning (most recent)
        messages.add(0, message);
    }

    public void updateMessage(MessageDisplayDto updated) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).messageId().equals(updated.messageId())) {
                messages.set(i, updated);
                break;
            }
        }
    }

    public void clearMessages() {
        messages.clear();
        currentPage.set(0);
        totalPages.set(0);
        hasMore.set(true);
    }

    public void setLoading(boolean value) { loading.set(value); }
    public void setError(String value) { error.set(value); }
}
```

### 4.4 ContactStore.java

```java
package com.example.chatapp.client.store;

import com.example.chatapp.client.dto.response.ContactDisplayResponse;
import javafx.beans.property.*;
import javafx.collections.*;
import java.util.UUID;

public class ContactStore {

    private final ObservableList<ContactDisplayResponse> contacts = FXCollections.observableArrayList();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty error = new SimpleStringProperty();

    // Property getters
    public ObservableList<ContactDisplayResponse> getContacts() { return contacts; }
    public BooleanProperty loadingProperty() { return loading; }
    public StringProperty errorProperty() { return error; }

    // Actions
    public void setContacts(java.util.List<ContactDisplayResponse> contactList) {
        contacts.setAll(contactList);
    }

    public void addContact(ContactDisplayResponse contact) {
        contacts.add(contact);
    }

    public void updateContact(ContactDisplayResponse updated) {
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).contactUserId().equals(updated.contactUserId())) {
                contacts.set(i, updated);
                break;
            }
        }
    }

    public void removeContact(UUID contactUserId) {
        contacts.removeIf(c -> c.contactUserId().equals(contactUserId));
    }

    public void setLoading(boolean value) { loading.set(value); }
    public void setError(String value) { error.set(value); }
}
```

---

## 5. WebSocket Integration

### 5.1 WebSocketService.java

```java
package com.example.chatapp.client.service;

import com.example.chatapp.client.api.TokenStorage;
import com.example.chatapp.client.dto.response.*;
import com.example.chatapp.client.store.*;
import com.example.chatapp.client.util.JsonMapper;
import javafx.application.Platform;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import java.lang.reflect.Type;
import java.util.*;

public class WebSocketService {

    private static final String WS_URL = "http://localhost:8080/ws";

    private final TokenStorage tokenStorage;
    private final ChatStore chatStore;
    private final MessageStore messageStore;
    private final ContactStore contactStore;

    private WebSocketStompClient stompClient;
    private StompSession session;
    private String currentUsername;

    public WebSocketService(
            TokenStorage tokenStorage,
            ChatStore chatStore,
            MessageStore messageStore,
            ContactStore contactStore) {
        this.tokenStorage = tokenStorage;
        this.chatStore = chatStore;
        this.messageStore = messageStore;
        this.contactStore = contactStore;
    }

    public void connect(String username) {
        this.currentUsername = username;

        List<Transport> transports = List.of(
            new WebSocketTransport(new StandardWebSocketClient())
        );
        SockJsClient sockJsClient = new SockJsClient(transports);

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompHeaders headers = new StompHeaders();
        headers.add("Authorization", "Bearer " + tokenStorage.getToken());

        stompClient.connectAsync(WS_URL, new WebSocketHttpHeaders(), headers,
            new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders headers) {
                    WebSocketService.this.session = session;
                    subscribeToChannels();
                }

                @Override
                public void handleException(StompSession session, StompCommand command,
                        StompHeaders headers, byte[] payload, Throwable exception) {
                    exception.printStackTrace();
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    exception.printStackTrace();
                    // Attempt reconnection
                    reconnect();
                }
            });
    }

    private void subscribeToChannels() {
        // New chat notifications
        session.subscribe("/user/" + currentUsername + "/queue/newChat",
            new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return ChatDisplayDto.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    Platform.runLater(() -> chatStore.addChat((ChatDisplayDto) payload));
                }
            });

        // Chat removed notifications
        session.subscribe("/user/" + currentUsername + "/queue/chatRemoved",
            new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return UUID.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    Platform.runLater(() -> chatStore.removeChat((UUID) payload));
                }
            });

        // Contact updates
        session.subscribe("/user/" + currentUsername + "/queue/contactUpdates",
            new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return ContactNotificationDto.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    // Handle contact notifications
                    // Could trigger a contacts refresh
                }
            });
    }

    public void subscribeToChat(UUID chatId) {
        if (session == null || !session.isConnected()) return;

        // Messages
        session.subscribe("/topic/chat/" + chatId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageDisplayDto.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                MessageDisplayDto msg = (MessageDisplayDto) payload;
                Platform.runLater(() -> {
                    // Check if message exists (update) or is new
                    boolean exists = messageStore.getMessages().stream()
                        .anyMatch(m -> m.messageId().equals(msg.messageId()));
                    if (exists) {
                        messageStore.updateMessage(msg);
                    } else {
                        messageStore.addMessage(msg);
                    }
                });
            }
        });

        // Member updates
        session.subscribe("/topic/chat/" + chatId + "/members", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MemberUpdateDto.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                Platform.runLater(() -> {
                    // Refresh chat members
                });
            }
        });

        // Chat property updates
        session.subscribe("/topic/chat/" + chatId + "/updates", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatDisplayDto.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                Platform.runLater(() -> chatStore.updateChat((ChatDisplayDto) payload));
            }
        });
    }

    private void reconnect() {
        // Implement exponential backoff reconnection
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                if (currentUsername != null) {
                    connect(currentUsername);
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        currentUsername = null;
    }
}
```

---

## 6. UI Controller Binding Examples

### 6.1 LoginController.java

```java
package com.example.chatapp.client.ui.controller;

import com.example.chatapp.client.service.AuthService;
import com.example.chatapp.client.store.AuthStore;
import com.example.chatapp.client.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final AuthService authService;
    private final AuthStore authStore;
    private final NavigationManager navigation;

    public LoginController(AuthService authService, AuthStore authStore,
                          NavigationManager navigation) {
        this.authService = authService;
        this.authStore = authStore;
        this.navigation = navigation;
    }

    @FXML
    public void initialize() {
        // Bind loading state
        loadingIndicator.visibleProperty().bind(authStore.loadingProperty());
        loginButton.disableProperty().bind(authStore.loadingProperty());

        // Bind error display
        errorLabel.textProperty().bind(authStore.errorProperty());
        errorLabel.visibleProperty().bind(authStore.errorProperty().isNotEmpty());

        // Navigate on successful login
        authStore.loggedInProperty().addListener((obs, wasLoggedIn, isLoggedIn) -> {
            if (isLoggedIn) {
                navigation.navigateTo("Home");
            }
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            authStore.setError("Please fill in all fields");
            return;
        }

        authStore.clearError();
        authService.login(username, password);
    }

    @FXML
    private void goToRegister() {
        navigation.navigateTo("Register");
    }
}
```

### 6.2 ChatController.java (Messages View)

```java
package com.example.chatapp.client.ui.controller;

import com.example.chatapp.client.dto.response.MessageDisplayDto;
import com.example.chatapp.client.service.MessageService;
import com.example.chatapp.client.store.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ChatController {

    @FXML private ListView<MessageDisplayDto> messageListView;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Label chatNameLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final ChatStore chatStore;
    private final MessageStore messageStore;
    private final MessageService messageService;

    public ChatController(ChatStore chatStore, MessageStore messageStore,
                         MessageService messageService) {
        this.chatStore = chatStore;
        this.messageStore = messageStore;
        this.messageService = messageService;
    }

    @FXML
    public void initialize() {
        // Bind messages list
        messageListView.setItems(messageStore.getMessages());

        // Custom cell factory for message rendering
        messageListView.setCellFactory(lv -> new MessageListCell());

        // Bind loading indicator
        loadingIndicator.visibleProperty().bind(messageStore.loadingProperty());

        // Disable send when loading
        sendButton.disableProperty().bind(
            messageStore.loadingProperty()
            .or(messageInput.textProperty().isEmpty())
        );

        // Update header when active chat changes
        chatStore.activeChatProperty().addListener((obs, oldChat, newChat) -> {
            if (newChat != null) {
                String name = newChat.chatType().equals("GROUP")
                    ? newChat.groupName()
                    : getOtherMemberName(newChat);
                chatNameLabel.setText(name);

                // Load messages for new chat
                messageService.loadMessages(newChat.chatId(), 0);
            }
        });

        // Infinite scroll for pagination
        messageListView.setOnScroll(event -> {
            if (event.getDeltaY() < 0 && isAtBottom() && messageStore.hasMoreProperty().get()) {
                int nextPage = messageStore.currentPageProperty().get() + 1;
                messageService.loadMessages(
                    chatStore.getActiveChat().chatId(), nextPage);
            }
        });
    }

    @FXML
    private void handleSendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || chatStore.getActiveChat() == null) return;

        messageService.sendTextMessage(
            chatStore.getActiveChat().chatId(),
            content
        );
        messageInput.clear();
    }

    private boolean isAtBottom() {
        // Implementation to check scroll position
        return true;
    }

    private String getOtherMemberName(ChatDisplayDto chat) {
        // Get the other member's name in P2P chat
        return chat.members().stream()
            .filter(m -> !m.userId().equals(getCurrentUserId()))
            .findFirst()
            .map(MemberDisplayDto::username)
            .orElse("Chat");
    }
}
```

---

## 7. Navigation Flow

```
┌──────────────┐
│   Splash     │
│   Screen     │
└──────┬───────┘
       │ Check token
       ▼
┌──────────────────────────────────┐
│                                  │
│  ┌─────────┐     ┌────────────┐  │
│  │  Login  │◄───►│  Register  │  │
│  └────┬────┘     └────────────┘  │
│       │                          │
└───────┼──────────────────────────┘
        │ Auth success
        ▼
┌──────────────────────────────────────────────┐
│                   Home View                   │
│  ┌─────────────────────────────────────────┐ │
│  │           Chat List (Left)              │ │
│  │  ┌─────────────────────────────────┐    │ │
│  │  │ Chat 1                          │    │ │
│  │  │ Chat 2                          │◄───┼─┼─── WebSocket: /queue/newChat
│  │  │ Chat 3                          │    │ │
│  │  └─────────────────────────────────┘    │ │
│  └─────────────────────────────────────────┘ │
│                      │                        │
│                      │ Select chat            │
│                      ▼                        │
│  ┌─────────────────────────────────────────┐ │
│  │         Chat/Messages View (Right)      │ │
│  │  ┌─────────────────────────────────┐    │ │
│  │  │ Message 1                       │◄───┼─┼─── WebSocket: /topic/chat/{id}
│  │  │ Message 2                       │    │ │
│  │  │ Message 3                       │    │ │
│  │  └─────────────────────────────────┘    │ │
│  │  [Type message...] [Send]               │ │
│  └─────────────────────────────────────────┘ │
│                                              │
│  Navigation Bar:                             │
│  [Chats] [Contacts] [Groups] [Settings]      │
└──────────────────────────────────────────────┘
        │           │           │
        ▼           ▼           ▼
┌─────────────┐ ┌─────────┐ ┌──────────┐
│  Contacts   │ │ Create  │ │ Settings │
│    View     │ │  Group  │ │   View   │
│             │ │  View   │ │          │
│ - List      │ │         │ │ - Profile│
│ - Add       │ │ - Name  │ │ - Logout │
│ - Search    │ │ - Image │ │          │
│ - Delete    │ │ - Members│ │          │
└─────────────┘ └─────────┘ └──────────┘
```

---

## 8. Required Dependencies (pom.xml additions)

```xml
<!-- JavaFX -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21</version>
</dependency>

<!-- Jackson for JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.16.0</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
    <version>2.16.0</version>
</dependency>

<!-- Spring WebSocket Client (for STOMP) -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-websocket</artifactId>
    <version>6.1.0</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-messaging</artifactId>
    <version>6.1.0</version>
</dependency>
```

---

## 9. Missing Pieces & Ambiguities Found

### Issues Identified:

1. **No DTO Validation Annotations**: The backend DTOs don't have `@NotNull`, `@Size`, etc. annotations. The JavaFX client should implement client-side validation.

2. **Inconsistent Error Handling**: `AuthService.register()` throws `RuntimeException` instead of `AppException` for "Username already taken" - won't be handled by `GlobalExceptionHandler`.

3. **Missing Response DTOs**:

   - `createChat()` returns `ChatDisplayDto` in service but `ResponseEntity<?>` with String in controller
   - Same issue with `addMember()`, `updateContact()`, `addContact()`

4. **Phone Number Field Typo**: In `User.java`, column is named `phone_name` instead of `phone_number`.

5. **Unused DTOs**:

   - `DeleteMessageRequest` is empty and not used (delete uses path variable)
   - `MemberUpdateDto` is used only for WebSocket notifications

6. **No Rate Limiting**: No protection against API abuse.

7. **No Refresh Token**: JWT has 24-hour expiry with no refresh mechanism.

---

## 10. Frontend Contract - Stable JSON Shapes

### Authentication

```json
// POST /api/auth/login, /api/auth/register
// Response:
{ "token": "string" }
```

### User

```json
// UserResponse
{
  "ID": "uuid",
  "name": "string",
  "username": "string",
  "phoneNumber": "string"
}
```

### Chat

```json
// ChatDisplayDto
{
  "chatId": "uuid",
  "chatType": "P2P|GROUP",
  "groupName": "string|null",
  "groupImage": "string|null",
  "members": [MemberDisplayDto]
}

// MemberDisplayDto
{
  "userId": "uuid",
  "username": "string",
  "role": "ADMIN|MEMBER"
}
```

### Message

```json
// MessageDisplayDto
{
  "messageId": "uuid",
  "sender": {
    "senderId": "uuid",
    "username": "string"
  },
  "messageType": "TEXT|IMAGE|AUDIO|VIDEO",
  "content": "string",
  "mediaUrl": "string|null",
  "timestamp": "2025-12-19T20:30:00",
  "isEdited": boolean,
  "isDeleted": boolean
}

// Page wrapper
{
  "content": [MessageDisplayDto],
  "totalElements": number,
  "totalPages": number,
  "size": number,
  "number": number
}
```

### Contact

```json
// ContactDisplayResponse
{
  "ID": "uuid",
  "contactUserId": "uuid",
  "displayName": "string",
  "contactUsername": "string",
  "contactPhoneNumber": "string"
}

// ContactMatchResponse
{
  "id": "uuid",
  "userName": "string",
  "name": "string",
  "phoneNumber": "string"
}
```

### Error Response

```json
{
  "timestamp": "datetime",
  "status": number,
  "error": "string",
  "message": "string"
}
```
