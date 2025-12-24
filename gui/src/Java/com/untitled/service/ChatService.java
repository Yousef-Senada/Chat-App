package com.untitled.service;

import com.untitled.api.endpoints.ChatsApi;
import com.untitled.dto.response.ChatDisplayDto;
import com.untitled.dto.response.ContactDisplayResponse;
import com.untitled.store.AuthStore;
import com.untitled.store.ChatStore;
import com.untitled.store.ContactStore;
import com.untitled.util.ErrorHandler;

import javafx.application.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatService {

  private final ChatsApi chatsApi;
  private final ChatStore chatStore;
  private final AuthStore authStore;
  private final ContactStore contactStore;
  private final Map<UUID, String> contactNamesCache;

  public ChatService(ChatsApi chatsApi, ChatStore chatStore, AuthStore authStore, ContactStore contactStore) {
    this.chatsApi = chatsApi;
    this.chatStore = chatStore;
    this.authStore = authStore;
    this.contactStore = contactStore;
    this.contactNamesCache = new HashMap<>();
  }

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

  public void createP2PChat(UUID otherUserId) {
    chatStore.setLoading(true);
    chatStore.clearError();

    chatsApi.createP2PChat(otherUserId)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
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

  public void createGroupChat(String groupName, String groupImage, List<UUID> memberIds) {
    chatStore.setLoading(true);
    chatStore.clearError();

    chatsApi.createGroupChat(groupName, groupImage, memberIds)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
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

  public void setActiveChat(ChatDisplayDto chat) {
    chatStore.setActiveChat(chat);

    if (chat != null && (chat.members() == null || chat.members().isEmpty())) {
      loadChatMembers(chat.chatId());
    }
  }

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

  public void addMember(UUID chatId, UUID memberId) {
    chatStore.setLoading(true);
    chatStore.clearError();

    chatsApi.addMembers(chatId, List.of(memberId))
        .thenAccept(response -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
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

  public void removeMember(UUID chatId, UUID memberId) {
    chatStore.setLoading(true);
    chatStore.clearError();

    chatsApi.removeMembers(chatId, List.of(memberId))
        .thenAccept(response -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
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

  public void updateContactNamesCache() {
    contactNamesCache.clear();
    for (ContactDisplayResponse contact : contactStore.getContacts()) {
      if (contact.contactUserId() != null && contact.displayName() != null) {
        contactNamesCache.put(contact.contactUserId(), contact.displayName());
      }
    }
    System.out.println("Contact names cache updated with " + contactNamesCache.size() + " entries");
  }

  public String getDisplayNameForUser(UUID userId, String fallbackName) {
    String contactName = contactNamesCache.get(userId);
    return contactName != null ? contactName : fallbackName;
  }

  public ChatStore getStore() {
    return chatStore;
  }

  private String extractErrorMessage(Throwable throwable) {
    return ErrorHandler.extractMessage(throwable);
  }
}
