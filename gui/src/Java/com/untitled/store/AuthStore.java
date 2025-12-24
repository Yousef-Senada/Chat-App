package com.untitled.store;

import com.untitled.api.TokenStorage;
import com.untitled.dto.response.UserResponse;
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

  public ObjectProperty<UserResponse> currentUserProperty() {
    return currentUser;
  }

  public BooleanProperty loggedInProperty() {
    return loggedIn;
  }

  public BooleanProperty loadingProperty() {
    return loading;
  }

  public StringProperty errorProperty() {
    return error;
  }

  public UserResponse getCurrentUser() {
    return currentUser.get();
  }

  public boolean isLoggedIn() {
    return loggedIn.get();
  }

  public boolean isLoading() {
    return loading.get();
  }

  public String getError() {
    return error.get();
  }

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
    error.set(null);
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

  public boolean hasToken() {
    return tokenStorage.hasToken();
  }
}
