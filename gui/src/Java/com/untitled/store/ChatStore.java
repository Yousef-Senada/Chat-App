package com.untitled.store;

import com.untitled.dto.response.ChatDisplayDto;
import com.untitled.dto.response.MemberDisplayDto;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ChatStore {

  private final ObservableList<ChatDisplayDto> chats = FXCollections.observableArrayList();
  private final ObjectProperty<ChatDisplayDto> activeChat = new SimpleObjectProperty<>();
  private final ObservableList<MemberDisplayDto> activeChatMembers = FXCollections.observableArrayList();
  private final BooleanProperty loading = new SimpleBooleanProperty(false);
  private final StringProperty error = new SimpleStringProperty();

  public ObservableList<ChatDisplayDto> getChats() {
    return chats;
  }

  public ObservableList<MemberDisplayDto> getActiveChatMembers() {
    return activeChatMembers;
  }

  public ObjectProperty<ChatDisplayDto> activeChatProperty() {
    return activeChat;
  }

  public BooleanProperty loadingProperty() {
    return loading;
  }

  public StringProperty errorProperty() {
    return error;
  }

  public ChatDisplayDto getActiveChat() {
    return activeChat.get();
  }

  public boolean isLoading() {
    return loading.get();
  }

  public String getError() {
    return error.get();
  }

  public void setChats(List<ChatDisplayDto> newChats) {
    chats.clear();
    chats.addAll(newChats);
  }

  public void addChat(ChatDisplayDto chat) {
    chats.add(0, chat);
  }

  public void removeChat(ChatDisplayDto chat) {
    chats.remove(chat);
  }

  public void setActiveChat(ChatDisplayDto chat) {
    activeChat.set(chat);
    if (chat != null && chat.members() != null) {
      activeChatMembers.setAll(chat.members());
    } else {
      activeChatMembers.clear();
    }
  }

  public void setActiveChatMembers(List<MemberDisplayDto> members) {
    activeChatMembers.clear();
    activeChatMembers.addAll(members);
  }

  public void clearActiveChat() {
    activeChat.set(null);
    activeChatMembers.clear();
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
    chats.clear();
    activeChat.set(null);
    activeChatMembers.clear();
    error.set(null);
  }
}
