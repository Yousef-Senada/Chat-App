package com.untitled.service;

import com.untitled.api.ApiException;
import com.untitled.api.TokenStorage;
import com.untitled.api.endpoints.AuthApi;
import com.untitled.api.endpoints.UsersApi;
import com.untitled.dto.response.UserResponse;
import com.untitled.store.AuthStore;
import com.untitled.util.ErrorHandler;

import javafx.application.Platform;

/**
 * Service class for authentication operations.
 * Orchestrates API calls and state updates.
 */
public class AuthService {

  private final AuthApi authApi;
  private final UsersApi usersApi;
  private final AuthStore authStore;
  private final TokenStorage tokenStorage;

  public AuthService(AuthApi authApi, UsersApi usersApi, AuthStore authStore, TokenStorage tokenStorage) {
    this.authApi = authApi;
    this.usersApi = usersApi;
    this.authStore = authStore;
    this.tokenStorage = tokenStorage;
  }

  /**
   * Logs in a user with username and password.
   * Updates AuthStore with result.
   */
  public void login(String username, String password) {
    authStore.setLoading(true);
    authStore.clearError();

    authApi.login(username, password)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            tokenStorage.setToken(response.token());
            authStore.setToken(response.token());
            authStore.setLoading(false);
            // Load user profile after successful login
            loadProfile();
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            authStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            authStore.setError(errorMessage);
          });
          return null;
        });
  }

  /**
   * Registers a new user.
   * Updates AuthStore with result.
   */
  public void register(String name, String username, String phoneNumber, String password) {
    authStore.setLoading(true);
    authStore.clearError();

    authApi.register(name, username, phoneNumber, password)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            tokenStorage.setToken(response.token());
            authStore.setToken(response.token());
            authStore.setLoading(false);
            // Load user profile after successful registration
            loadProfile();
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            authStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            authStore.setError(errorMessage);
          });
          return null;
        });
  }

  /**
   * Loads the current user's profile.
   */
  public void loadProfile() {
    if (!authStore.hasToken()) {
      return;
    }

    usersApi.getProfile()
        .thenAccept(user -> {
          Platform.runLater(() -> {
            authStore.setCurrentUser(user);
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            // If profile load fails with 401, token is invalid
            if (throwable.getCause() instanceof ApiException apiEx) {
              if (apiEx.isUnauthorized()) {
                logout();
              }
            }
          });
          return null;
        });
  }

  /**
   * Logs out the current user.
   */
  public void logout() {
    authStore.logout();
  }

  /**
   * Checks if user is currently logged in.
   */
  public boolean isLoggedIn() {
    return authStore.isLoggedIn();
  }

  /**
   * Gets the current user.
   */
  public UserResponse getCurrentUser() {
    return authStore.getCurrentUser();
  }

  private String extractErrorMessage(Throwable throwable) {
    return ErrorHandler.extractMessage(throwable);
  }
}
