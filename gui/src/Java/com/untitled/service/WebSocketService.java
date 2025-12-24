package com.untitled.service;

import com.untitled.api.TokenStorage;
import com.untitled.dto.response.MessageDisplayDto;
import com.untitled.store.ChatStore;
import com.untitled.store.MessageStore;
import com.untitled.util.JsonMapper;

import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebSocketService {

  private static final String WS_URL = "ws://localhost:8080/ws";

  private final TokenStorage tokenStorage;
  private final MessageStore messageStore;
  private final ChatStore chatStore;
  private WebSocket webSocket;
  private UUID currentChatId;
  private boolean isConnected = false;

  public WebSocketService(TokenStorage tokenStorage, MessageStore messageStore, ChatStore chatStore) {
    this.tokenStorage = tokenStorage;
    this.messageStore = messageStore;
    this.chatStore = chatStore;
  }

  public CompletableFuture<Void> connect() {
    if (isConnected && webSocket != null) {
      System.out.println("WebSocket already connected");
      return CompletableFuture.completedFuture(null);
    }

    String token = tokenStorage.getToken();
    if (token == null || token.isEmpty()) {
      System.out.println("No auth token available for WebSocket connection");
      return CompletableFuture.failedFuture(new IllegalStateException("No auth token"));
    }

    System.out.println("Connecting to WebSocket at: " + WS_URL);

    HttpClient client = HttpClient.newHttpClient();

    CompletableFuture<Void> connectFuture = new CompletableFuture<>();

    try {
      webSocket = client.newWebSocketBuilder()
          .header("Authorization", "Bearer " + token)
          .buildAsync(URI.create(WS_URL), new WebSocket.Listener() {
            private StringBuilder messageBuffer = new StringBuilder();

            @Override
            public void onOpen(WebSocket webSocket) {
              System.out.println("WebSocket connection opened");
              isConnected = true;
              connectFuture.complete(null);
              webSocket.request(1);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
              messageBuffer.append(data);

              if (last) {
                String message = messageBuffer.toString();
                messageBuffer = new StringBuilder();
                handleMessage(message);
              }

              webSocket.request(1);
              return null;
            }

            @Override
            public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
              webSocket.request(1);
              return null;
            }

            @Override
            public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
              webSocket.request(1);
              return null;
            }

            @Override
            public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
              webSocket.request(1);
              return null;
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
              System.out.println("WebSocket closed: " + statusCode + " - " + reason);
              isConnected = false;
              webSocket.request(1);
              return null;
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
              System.out.println("WebSocket error: " + error.getMessage());
              error.printStackTrace();
              isConnected = false;
              if (!connectFuture.isDone()) {
                connectFuture.completeExceptionally(error);
              }
            }
          }).join();

    } catch (Exception e) {
      System.out.println("Failed to connect WebSocket: " + e.getMessage());
      e.printStackTrace();
      connectFuture.completeExceptionally(e);
    }

    return connectFuture;
  }

  public void subscribeToChat(UUID chatId) {
    this.currentChatId = chatId;

    if (webSocket != null && isConnected) {
      try {
        String subscribeMessage = "{\"action\":\"SUBSCRIBE\",\"chatId\":\"" + chatId.toString() + "\"}";
        webSocket.sendText(subscribeMessage, true);
        System.out.println("Subscribed to chat: " + chatId);
      } catch (Exception e) {
        System.out.println("Failed to subscribe to chat: " + e.getMessage());
      }
    }
  }

  public void unsubscribeFromChat() {
    if (currentChatId != null && webSocket != null && isConnected) {
      try {
        String unsubscribeMessage = "{\"action\":\"UNSUBSCRIBE\",\"chatId\":\"" + currentChatId.toString() + "\"}";
        webSocket.sendText(unsubscribeMessage, true);
        System.out.println("Unsubscribed from chat: " + currentChatId);
      } catch (Exception e) {
        System.out.println("Failed to unsubscribe from chat: " + e.getMessage());
      }
    }
    this.currentChatId = null;
  }

  private void handleMessage(String message) {
    System.out.println("WebSocket message received: " + message);

    try {
      Map<String, Object> data = JsonMapper.parseJson(message);
      String type = JsonMapper.getString(data, "type");

      if ("MESSAGE".equals(type)) {
        handleNewMessage(data);
      } else if ("MESSAGE_UPDATED".equals(type)) {
        handleMessageUpdate(data);
      } else if ("MESSAGE_DELETED".equals(type)) {
        handleMessageDelete(data);
      } else if ("TYPING".equals(type)) {
        handleTypingIndicator(data);
      }
    } catch (Exception e) {
      System.out.println("Failed to parse WebSocket message: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void handleNewMessage(Map<String, Object> data) {
    try {
      Map<String, Object> messageData = JsonMapper.getMap(data, "message");
      if (messageData == null) {
        messageData = data;
      }

      MessageDisplayDto message = MessageDisplayDto.fromMap(messageData);

      UUID messageChatId = JsonMapper.getUUID(messageData, "chatId");
      if (messageChatId != null && currentChatId != null && messageChatId.equals(currentChatId)) {
        Platform.runLater(() -> {
          boolean exists = messageStore.getMessages().stream()
              .anyMatch(m -> m.messageId() != null && m.messageId().equals(message.messageId()));

          if (!exists) {
            messageStore.addMessage(message);
            System.out.println("New message added via WebSocket");
          }
        });
      }
    } catch (Exception e) {
      System.out.println("Failed to handle new message: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void handleMessageUpdate(Map<String, Object> data) {
    try {
      Map<String, Object> messageData = JsonMapper.getMap(data, "message");
      if (messageData == null) {
        messageData = data;
      }

      MessageDisplayDto updatedMessage = MessageDisplayDto.fromMap(messageData);

      Platform.runLater(() -> {
        messageStore.updateMessage(updatedMessage);
        System.out.println("Message updated via WebSocket");
      });
    } catch (Exception e) {
      System.out.println("Failed to handle message update: " + e.getMessage());
    }
  }

  private void handleMessageDelete(Map<String, Object> data) {
    try {
      UUID messageId = JsonMapper.getUUID(data, "messageId");
      if (messageId != null) {
        Platform.runLater(() -> {
          messageStore.removeMessage(messageId);
          System.out.println("Message deleted via WebSocket");
        });
      }
    } catch (Exception e) {
      System.out.println("Failed to handle message delete: " + e.getMessage());
    }
  }

  private void handleTypingIndicator(Map<String, Object> data) {
    System.out.println("Typing indicator received");
  }

  public void disconnect() {
    if (webSocket != null) {
      try {
        unsubscribeFromChat();
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client closing");
        System.out.println("WebSocket disconnected");
      } catch (Exception e) {
        System.out.println("Error closing WebSocket: " + e.getMessage());
      }
      isConnected = false;
      webSocket = null;
    }
  }

  public boolean isConnected() {
    return isConnected && webSocket != null;
  }

  public CompletableFuture<Void> reconnect() {
    disconnect();
    return connect();
  }
}
