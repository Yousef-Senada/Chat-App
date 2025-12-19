package com.untitled.service;

import com.untitled.api.ApiClient;
import com.untitled.api.TokenStorage;
import com.untitled.api.endpoints.AuthApi;
import com.untitled.api.endpoints.ChatsApi;
import com.untitled.api.endpoints.ContactsApi;
import com.untitled.api.endpoints.MessagesApi;
import com.untitled.api.endpoints.UsersApi;
import com.untitled.store.AuthStore;
import com.untitled.store.ChatStore;
import com.untitled.store.ContactStore;
import com.untitled.store.MessageStore;

/**
 * Service Locator pattern for dependency injection.
 * Provides singleton access to all services and stores.
 */
public class ServiceLocator {

  private static ServiceLocator instance;

  // Infrastructure
  private final TokenStorage tokenStorage;
  private final ApiClient apiClient;

  // API Endpoints
  private final AuthApi authApi;
  private final UsersApi usersApi;
  private final ChatsApi chatsApi;
  private final MessagesApi messagesApi;
  private final ContactsApi contactsApi;

  // Stores
  private final AuthStore authStore;
  private final ChatStore chatStore;
  private final MessageStore messageStore;
  private final ContactStore contactStore;

  // Services
  private final AuthService authService;
  private final ChatService chatService;
  private final MessageService messageService;
  private final ContactService contactService;

  private ServiceLocator() {
    // Initialize infrastructure
    this.tokenStorage = new TokenStorage();
    this.apiClient = new ApiClient(tokenStorage);

    // Initialize API endpoints
    this.authApi = new AuthApi(apiClient);
    this.usersApi = new UsersApi(apiClient);
    this.chatsApi = new ChatsApi(apiClient);
    this.messagesApi = new MessagesApi(apiClient);
    this.contactsApi = new ContactsApi(apiClient);

    // Initialize stores
    this.authStore = new AuthStore(tokenStorage);
    this.chatStore = new ChatStore();
    this.messageStore = new MessageStore();
    this.contactStore = new ContactStore();

    // Initialize services
    this.authService = new AuthService(authApi, usersApi, authStore, tokenStorage);
    this.chatService = new ChatService(chatsApi, chatStore, authStore);
    this.messageService = new MessageService(messagesApi, messageStore);
    this.contactService = new ContactService(contactsApi, contactStore);
  }

  public static synchronized ServiceLocator getInstance() {
    if (instance == null) {
      instance = new ServiceLocator();
    }
    return instance;
  }

  // Infrastructure getters
  public TokenStorage getTokenStorage() {
    return tokenStorage;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  // API getters
  public AuthApi getAuthApi() {
    return authApi;
  }

  public UsersApi getUsersApi() {
    return usersApi;
  }

  public ChatsApi getChatsApi() {
    return chatsApi;
  }

  public MessagesApi getMessagesApi() {
    return messagesApi;
  }

  public ContactsApi getContactsApi() {
    return contactsApi;
  }

  // Store getters
  public AuthStore getAuthStore() {
    return authStore;
  }

  public ChatStore getChatStore() {
    return chatStore;
  }

  public MessageStore getMessageStore() {
    return messageStore;
  }

  public ContactStore getContactStore() {
    return contactStore;
  }

  // Service getters
  public AuthService getAuthService() {
    return authService;
  }

  public ChatService getChatService() {
    return chatService;
  }

  public MessageService getMessageService() {
    return messageService;
  }

  public ContactService getContactService() {
    return contactService;
  }

  /**
   * Resets the singleton instance.
   * Useful for testing or when user logs out completely.
   */
  public static synchronized void reset() {
    if (instance != null) {
      instance.authStore.logout();
      instance.chatStore.clear();
      instance.messageStore.clear();
      instance.contactStore.clear();
    }
    instance = null;
  }
}
