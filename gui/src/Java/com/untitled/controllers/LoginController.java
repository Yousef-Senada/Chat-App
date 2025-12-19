package com.untitled.controllers;

import com.untitled.service.AuthService;
import com.untitled.service.ServiceLocator;
import com.untitled.store.AuthStore;
import com.untitled.NavigationManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

/**
 * Controller for the login view.
 * Handles user authentication via the AuthService.
 */
public class LoginController {

  @FXML
  private Button loginBtn;
  @FXML
  private Label createAccountLink;
  @FXML
  private Label forgotPasswordLink;
  @FXML
  private TextField usernameField;
  @FXML
  private PasswordField passwordField;
  @FXML
  private Label errorLabel;
  @FXML
  private ProgressIndicator loadingIndicator;

  private AuthService authService;
  private AuthStore authStore;

  @FXML
  public void initialize() {
    System.out.println("Login View Initialized");

    // Get services from ServiceLocator
    authService = ServiceLocator.getInstance().getAuthService();
    authStore = ServiceLocator.getInstance().getAuthStore();

    // Check if already logged in
    if (authStore.isLoggedIn() && authStore.getCurrentUser() != null) {
      System.out.println("User already logged in, navigating to Dashboard");
      NavigationManager.getInstance().navigateTo("views/Dashboard.fxml", "ChatApp - Dashboard");
      return;
    }

    // Bind loading indicator visibility to loading state
    if (loadingIndicator != null) {
      loadingIndicator.visibleProperty().bind(authStore.loadingProperty());
      loadingIndicator.managedProperty().bind(authStore.loadingProperty());
    }

    // Bind login button disable state to loading
    if (loginBtn != null) {
      loginBtn.disableProperty().bind(authStore.loadingProperty());
    }

    // Bind error label to error state
    if (errorLabel != null) {
      errorLabel.textProperty().bind(authStore.errorProperty());
      errorLabel.visibleProperty().bind(authStore.errorProperty().isNotEmpty());
      errorLabel.managedProperty().bind(authStore.errorProperty().isNotEmpty());
    }

    // Listen for successful login (when user profile is loaded)
    authStore.currentUserProperty().addListener((obs, oldUser, newUser) -> {
      if (newUser != null && !authStore.isLoading()) {
        System.out.println("User logged in successfully, navigating to Dashboard");
        NavigationManager.getInstance().navigateTo("views/Dashboard.fxml", "ChatApp - Dashboard");
      }
    });

    // Clear any previous errors when view loads
    authStore.clearError();
  }

  @FXML
  private void onLoginClick() {
    String username = usernameField != null ? usernameField.getText().trim() : "";
    String password = passwordField != null ? passwordField.getText() : "";

    // Validate input
    if (username.isEmpty()) {
      authStore.setError("Please enter your username");
      return;
    }

    if (password.isEmpty()) {
      authStore.setError("Please enter your password");
      return;
    }

    // Clear previous error and attempt login
    authStore.clearError();
    authService.login(username, password);
  }

  @FXML
  private void onCreateAccountClick() {
    authStore.clearError();
    NavigationManager.getInstance().navigateTo("views/Register.fxml", "Create Account");
  }

  @FXML
  private void onForgotPasswordClick() {
    authStore.clearError();
    NavigationManager.getInstance().navigateTo("views/ForgotPassword.fxml", "Forgot Password");
  }
}
