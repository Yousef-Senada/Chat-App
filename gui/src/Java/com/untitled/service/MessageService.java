package com.untitled.service;

import com.untitled.api.endpoints.MessagesApi;
import com.untitled.store.MessageStore;
import com.untitled.util.ErrorHandler;

import javafx.application.Platform;

import java.util.UUID;


public class MessageService {

  private static final int PAGE_SIZE = 20;

  private final MessagesApi messagesApi;
  private final MessageStore messageStore;

  public MessageService(MessagesApi messagesApi, MessageStore messageStore) {
    this.messagesApi = messagesApi;
    this.messageStore = messageStore;
  }

  public void loadMessages(UUID chatId) {
    loadMessages(chatId, 0);
  }

  public void loadMessages(UUID chatId, int page) {
    if (page == 0 || messageStore.isForDifferentChat(chatId)) {
      messageStore.clear();
    }

    messageStore.setLoading(true);
    messageStore.clearError();

    messagesApi.getChatMessages(chatId, page, PAGE_SIZE)
        .thenAccept(pageResponse -> {
          Platform.runLater(() -> {
            messageStore.setMessages(
                pageResponse.content(),
                chatId,
                pageResponse.pageNumber(),
                pageResponse.totalPages(),
                pageResponse.hasMore());
            messageStore.setLoading(false);
            System.out.println("Loaded " + pageResponse.content().size() + " messages, page " + page);
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            messageStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            messageStore.setError(errorMessage);
            System.out.println("Failed to load messages: " + errorMessage);
          });
          return null;
        });
  }

  public void loadMoreMessages() {
    UUID chatId = messageStore.getCurrentChatId();
    if (chatId == null || !messageStore.hasMore() || messageStore.isLoading()) {
      return;
    }

    int nextPage = messageStore.getCurrentPage() + 1;
    loadMessages(chatId, nextPage);
  }

  public void sendTextMessage(UUID chatId, String content) {
    if (content == null || content.trim().isEmpty()) {
      return;
    }

    messagesApi.sendTextMessage(chatId, content.trim())
        .thenAccept(response -> {
          Platform.runLater(() -> {
            loadMessages(chatId, 0);
            System.out.println("Message sent successfully");
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            String errorMessage = extractErrorMessage(throwable);
            messageStore.setError(errorMessage);
            System.out.println("Failed to send message: " + errorMessage);
          });
          return null;
        });
  }

  public void sendImageMessage(UUID chatId, String caption, String mediaUrl) {
    messagesApi.sendImageMessage(chatId, caption, mediaUrl)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            loadMessages(chatId, 0);
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            String errorMessage = extractErrorMessage(throwable);
            messageStore.setError(errorMessage);
          });
          return null;
        });
  }

  public void editMessage(UUID messageId, String newContent) {
    messagesApi.editMessage(messageId, newContent)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            UUID chatId = messageStore.getCurrentChatId();
            if (chatId != null) {
              loadMessages(chatId, 0);
            }
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            String errorMessage = extractErrorMessage(throwable);
            messageStore.setError(errorMessage);
          });
          return null;
        });
  }

  public void deleteMessage(UUID messageId) {
    messagesApi.deleteMessage(messageId)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            messageStore.removeMessage(messageId);
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            String errorMessage = extractErrorMessage(throwable);
            messageStore.setError(errorMessage);
          });
          return null;
        });
  }

  public MessageStore getStore() {
    return messageStore;
  }

  public void clearMessages() {
    messageStore.clear();
  }

  private String extractErrorMessage(Throwable throwable) {
    return ErrorHandler.extractMessage(throwable);
  }
}
