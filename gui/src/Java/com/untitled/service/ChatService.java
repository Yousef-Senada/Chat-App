package com.untitled.service;

import com.untitled.api.endpoints.ChatsApi;
import com.untitled.dto.response.ChatDisplayDto;
import com.untitled.store.AuthStore;
import com.untitled.store.ChatStore;
import com.untitled.util.ErrorHandler;

import javafx.application.Platform;

import java.util.List;
import java.util.UUID;

/**
 * Service class for chat operations.
 * Orchestrates API calls and state updates.
 */
public class ChatService {

  private final ChatsApi chatsApi;
  private final ChatStore chatStore;
  private final AuthStore authStore;

  public ChatService(ChatsApi chatsApi, ChatStore chatStore, AuthStore authStore) {
    this.chatsApi = chatsApi;
    this.chatStore = chatStore;
    this.authStore = authStore;
  }

  /**
   * Loads all chats for the current user.
   */
  public void loadChats() {
    chatStore.setLoading(true);
    chatStore.clearError();

    chatsApi.getUserChats()
        .thenAccept(chats -> {
          Platform.runLater(() -> {
            chatStore.setChats(chats);
            chatStore.setLoading(false);
            System.out.println("Loaded " + chats.size() + " chats");
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            chatStore.setError(errorMessage);
            System.out.println("Failed to load chats: " + errorMessage);
          });
          return null;
        });
  }

  /**
   * Creates a P2P chat with another user.
   */
  public void createP2PChat(UUID otherUserId) {
    chatStore.setLoading(true);
    chatStore.clearError();

    chatsApi.createP2PChat(otherUserId)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            // Reload chats to get the new chat
            loadChats();
            System.out.println("P2P chat created: " + response);
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            chatStore.setError(errorMessage);
          });
          return null;
        });
  }

  /**
   * Creates a group chat.
   */
  public void createGroupChat(String groupName, String groupImage, List<UUID> memberIds) {
    chatStore.setLoading(true);
    chatStore.clearError();

    chatsApi.createGroupChat(groupName, groupImage, memberIds)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            // Reload chats to get the new chat
            loadChats();
            System.out.println("Group chat created: " + response);
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            chatStore.setError(errorMessage);
          });
          return null;
        });
  }

  /**
   * Sets the active chat.
   */
  public void setActiveChat(ChatDisplayDto chat) {
    chatStore.setActiveChat(chat);

    // Load members if not already loaded
    if (chat != null && (chat.members() == null || chat.members().isEmpty())) {
      loadChatMembers(chat.chatId());
    }
  }

  /**
   * Loads members for a chat.
   */
  public void loadChatMembers(UUID chatId) {
    chatsApi.getChatMembers(chatId)
        .thenAccept(members -> {
          Platform.runLater(() -> {
            chatStore.setActiveChatMembers(members);
          });
        })
        .exceptionally(throwable -> {
          System.out.println("Failed to load chat members: " + throwable.getMessage());
          return null;
        });
  }

  /**
   * Leaves a chat.
   */
  public void leaveChat(UUID chatId) {
    if (authStore.getCurrentUser() == null) {
      chatStore.setError("User not logged in");
      return;
    }

    UUID currentUserId = authStore.getCurrentUser().ID();
    chatStore.setLoading(true);

    chatsApi.leaveChat(chatId, currentUserId)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            chatStore.clearActiveChat();
            loadChats();
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            chatStore.setError(errorMessage);
          });
          return null;
        });
  }

  /**
   * Adds a member to a chat.
   */
  public void addMember(UUID chatId, UUID memberId) {
    chatStore.setLoading(true);
    chatStore.clearError();

    chatsApi.addMembers(chatId, List.of(memberId))
        .thenAccept(response -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            // Reload members
            loadChatMembers(chatId);
            System.out.println("Member added: " + memberId);
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            chatStore.setError(errorMessage);
            System.out.println("Failed to add member: " + errorMessage);
          });
          return null;
        });
  }

  /**
   * Removes a member from a chat.
   */
  public void removeMember(UUID chatId, UUID memberId) {
    chatStore.setLoading(true);
    chatStore.clearError();

    chatsApi.removeMembers(chatId, List.of(memberId))
        .thenAccept(response -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            // Reload members
            loadChatMembers(chatId);
            System.out.println("Member removed: " + memberId);
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            chatStore.setError(errorMessage);
            System.out.println("Failed to remove member: " + errorMessage);
          });
          return null;
        });
  }

  /**
   * Gets the chat store for binding.
   */
  public ChatStore getStore() {
    return chatStore;
  }

  private String extractErrorMessage(Throwable throwable) {
    return ErrorHandler.extractMessage(throwable);
  }
}
