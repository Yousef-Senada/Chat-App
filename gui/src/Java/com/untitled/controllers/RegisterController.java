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
 * Controller for the registration view.
 * Handles new user registration via the AuthService.
 */
public class RegisterController {

  @FXML
  private Button registerBtn;
  @FXML
  private Label loginLink;
  @FXML
  private TextField nameField;
  @FXML
  private TextField usernameField;
  @FXML
  private TextField phoneField;
  @FXML
  private PasswordField passwordField;
  @FXML
  private PasswordField confirmPasswordField;
  @FXML
  private Label errorLabel;
  @FXML
  private ProgressIndicator loadingIndicator;

  private AuthService authService;
  private AuthStore authStore;

  @FXML
  public void initialize() {
    System.out.println("Register View Initialized");

    // Get services from ServiceLocator
    authService = ServiceLocator.getInstance().getAuthService();
    authStore = ServiceLocator.getInstance().getAuthStore();

    // Bind loading indicator visibility to loading state
    if (loadingIndicator != null) {
      loadingIndicator.visibleProperty().bind(authStore.loadingProperty());
      loadingIndicator.managedProperty().bind(authStore.loadingProperty());
    }

    // Bind register button disable state to loading
    if (registerBtn != null) {
      registerBtn.disableProperty().bind(authStore.loadingProperty());
    }

    // Bind error label to error state
    if (errorLabel != null) {
      errorLabel.textProperty().bind(authStore.errorProperty());
      errorLabel.visibleProperty().bind(authStore.errorProperty().isNotEmpty());
      errorLabel.managedProperty().bind(authStore.errorProperty().isNotEmpty());
    }

    // Listen for user profile being set after successful registration
    // We use currentUserProperty because loggedInProperty might already be true
    // from a previous session
    authStore.currentUserProperty().addListener((obs, oldUser, newUser) -> {
      if (newUser != null && !authStore.isLoading()) {
        System.out.println("User profile loaded after registration, navigating to Dashboard");
        NavigationManager.getInstance().navigateTo("views/Dashboard.fxml", "ChatApp - Dashboard");
      }
    });

    // Clear any previous errors when view loads
    authStore.clearError();
  }

  @FXML
  private void onRegisterClick() {
    String name = nameField != null ? nameField.getText().trim() : "";
    String username = usernameField != null ? usernameField.getText().trim() : "";
    String phone = phoneField != null ? phoneField.getText().trim() : "";
    String password = passwordField != null ? passwordField.getText() : "";
    String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : "";

    // Validate input
    if (name.isEmpty()) {
      authStore.setError("Please enter your name");
      return;
    }

    if (username.isEmpty()) {
      authStore.setError("Please enter a username");
      return;
    }

    if (username.length() < 3) {
      authStore.setError("Username must be at least 3 characters");
      return;
    }

    if (phone.isEmpty()) {
      authStore.setError("Please enter your phone number");
      return;
    }

    if (password.isEmpty()) {
      authStore.setError("Please enter a password");
      return;
    }

    if (password.length() < 6) {
      authStore.setError("Password must be at least 6 characters");
      return;
    }

    if (!password.equals(confirmPassword)) {
      authStore.setError("Passwords do not match");
      return;
    }

    // Clear previous error and attempt registration
    authStore.clearError();
    authService.register(name, username, phone, password);
  }

  @FXML
  private void onLoginLinkClick() {
    authStore.clearError();
    NavigationManager.getInstance().navigateTo("views/login.fxml", "Login");
  }
}
