package com.untitled.service;

import com.untitled.api.ApiException;
import com.untitled.api.TokenStorage;
import com.untitled.api.endpoints.AuthApi;
import com.untitled.api.endpoints.UsersApi;
import com.untitled.dto.response.UserResponse;
import com.untitled.store.AuthStore;
import com.untitled.util.ErrorHandler;

import javafx.application.Platform;

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

  public void login(String username, String password) {
    authStore.setLoading(true);
    authStore.clearError();

    authApi.login(username, password)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            tokenStorage.setToken(response.token());
            authStore.setToken(response.token());
            authStore.setLoading(false);
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

  public void register(String name, String username, String phoneNumber, String password) {
    authStore.setLoading(true);
    authStore.clearError();

    authApi.register(name, username, phoneNumber, password)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            tokenStorage.setToken(response.token());
            authStore.setToken(response.token());
            authStore.setLoading(false);
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
            if (throwable.getCause() instanceof ApiException apiEx) {
              if (apiEx.isUnauthorized()) {
                logout();
              }
            }
          });
          return null;
        });
  }

  public void logout() {
    authStore.logout();
  }

  public boolean isLoggedIn() {
    return authStore.isLoggedIn();
  }

  public UserResponse getCurrentUser() {
    return authStore.getCurrentUser();
  }

  private String extractErrorMessage(Throwable throwable) {
    return ErrorHandler.extractMessage(throwable);
  }
}
