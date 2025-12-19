package com.untitled.store;

import com.untitled.dto.response.MessageDisplayDto;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.UUID;

/**
 * Observable store for message state.
 * Uses JavaFX properties for UI binding.
 */
public class MessageStore {

  private final ObservableList<MessageDisplayDto> messages = FXCollections.observableArrayList();
  private final ObjectProperty<UUID> currentChatId = new SimpleObjectProperty<>();
  private final IntegerProperty currentPage = new SimpleIntegerProperty(0);
  private final IntegerProperty totalPages = new SimpleIntegerProperty(0);
  private final BooleanProperty hasMore = new SimpleBooleanProperty(false);
  private final BooleanProperty loading = new SimpleBooleanProperty(false);
  private final StringProperty error = new SimpleStringProperty();

  // Observable list getter
  public ObservableList<MessageDisplayDto> getMessages() {
    return messages;
  }

  // Property getters for binding
  public ObjectProperty<UUID> currentChatIdProperty() {
    return currentChatId;
  }

  public IntegerProperty currentPageProperty() {
    return currentPage;
  }

  public IntegerProperty totalPagesProperty() {
    return totalPages;
  }

  public BooleanProperty hasMoreProperty() {
    return hasMore;
  }

  public BooleanProperty loadingProperty() {
    return loading;
  }

  public StringProperty errorProperty() {
    return error;
  }

  // Value getters
  public UUID getCurrentChatId() {
    return currentChatId.get();
  }

  public int getCurrentPage() {
    return currentPage.get();
  }

  public int getTotalPages() {
    return totalPages.get();
  }

  public boolean hasMore() {
    return hasMore.get();
  }

  public boolean isLoading() {
    return loading.get();
  }

  public String getError() {
    return error.get();
  }

  // Actions
  public void setMessages(List<MessageDisplayDto> newMessages, UUID chatId, int page, int total, boolean more) {
    currentChatId.set(chatId);
    currentPage.set(page);
    totalPages.set(total);
    hasMore.set(more);

    if (page == 0) {
      // First page - replace all messages
      messages.clear();
    }
    // Add messages to the end (older messages for pagination)
    messages.addAll(newMessages);
  }

  public void addMessage(MessageDisplayDto message) {
    // Add new messages at the beginning (most recent first)
    messages.add(0, message);
  }

  public void updateMessage(UUID messageId, MessageDisplayDto updatedMessage) {
    for (int i = 0; i < messages.size(); i++) {
      if (messages.get(i).messageId().equals(messageId)) {
        messages.set(i, updatedMessage);
        break;
      }
    }
  }

  public void removeMessage(UUID messageId) {
    messages.removeIf(m -> m.messageId().equals(messageId));
  }

  public void setLoading(boolean value) {
    loading.set(value);
  }

  public void setError(String value) {
    error.set(value);
  }

  public void clearError() {
    error.set(null);
  }

  public void clear() {
    messages.clear();
    currentChatId.set(null);
    currentPage.set(0);
    totalPages.set(0);
    hasMore.set(false);
    error.set(null);
  }

  /**
   * Checks if messages are for a different chat.
   */
  public boolean isForDifferentChat(UUID chatId) {
    UUID current = currentChatId.get();
    return current == null || !current.equals(chatId);
  }
}
