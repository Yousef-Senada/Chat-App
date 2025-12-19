package com.untitled.controllers;

import com.untitled.dto.response.UserResponse;
import com.untitled.service.AuthService;
import com.untitled.service.ServiceLocator;
import com.untitled.store.AuthStore;
import com.untitled.NavigationManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Controller for the settings view.
 * Displays and manages user profile settings.
 */
public class SettingsController {

  @FXML
  private Circle profileAvatar;
  @FXML
  private TextField nameField;
  @FXML
  private TextField statusField;
  @FXML
  private TextField emailField;

  @FXML
  private Button lightThemeBtn;
  @FXML
  private Button darkThemeBtn;

  @FXML
  private CheckBox messageNotifCheckbox;
  @FXML
  private CheckBox soundCheckbox;
  @FXML
  private CheckBox desktopNotifCheckbox;
  @FXML
  private CheckBox onlineStatusCheckbox;
  @FXML
  private CheckBox readReceiptsCheckbox;

  private AuthService authService;
  private AuthStore authStore;

  @FXML
  public void initialize() {
    System.out.println("Settings View Initialized");

    // Get services from ServiceLocator
    authService = ServiceLocator.getInstance().getAuthService();
    authStore = ServiceLocator.getInstance().getAuthStore();

    loadUserProfile();

    // Listen for user changes
    authStore.currentUserProperty().addListener((obs, oldUser, newUser) -> {
      if (newUser != null) {
        updateProfileDisplay(newUser);
      }
    });
  }

  private void loadUserProfile() {
    UserResponse currentUser = authStore.getCurrentUser();
    if (currentUser != null) {
      updateProfileDisplay(currentUser);
    } else {
      // Load profile if not already loaded
      authService.loadProfile();
      // Set defaults while loading
      nameField.setText("Loading...");
      statusField.setText("Available");
      emailField.setText("");
    }
  }

  private void updateProfileDisplay(UserResponse user) {
    nameField.setText(user.name() != null ? user.name() : "");
    statusField.setText("Available");
    // Use username as email since API doesn't return email
    emailField.setText(user.username() != null ? user.username() : "");

    // Generate avatar color from username
    String avatarColor = generateAvatarColor(user.username());
    profileAvatar.setFill(Color.web(avatarColor));
  }

  private String generateAvatarColor(String username) {
    if (username == null || username.isEmpty()) {
      return "#8B5CF6";
    }
    // Generate a consistent color based on username
    String[] colors = { "#8B5CF6", "#3B82F6", "#22C55E", "#EF4444", "#F59E0B", "#EC4899", "#06B6D4", "#10B981" };
    int hash = Math.abs(username.hashCode());
    return colors[hash % colors.length];
  }

  @FXML
  private void onSaveProfile() {
    String name = nameField.getText();
    String status = statusField.getText();

    // Note: Profile update API not implemented in backend documentation
    // Just show confirmation for now
    System.out.println("Profile saved: " + name + ", " + status);
    showSaveConfirmation();
  }

  private void showSaveConfirmation() {
    System.out.println("Settings saved successfully!");
  }

  @FXML
  private void onLightThemeClick() {
    lightThemeBtn.setStyle("-fx-background-color: #8B5CF6;");
    darkThemeBtn.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #374151; -fx-border-color: #E5E7EB;");
    System.out.println("Light theme selected");
  }

  @FXML
  private void onDarkThemeClick() {
    darkThemeBtn.setStyle("-fx-background-color: #8B5CF6;");
    lightThemeBtn.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #374151; -fx-border-color: #E5E7EB;");
    System.out.println("Dark theme selected");
  }

  @FXML
  private void onChatsClick() {
    NavigationManager.getInstance().navigateTo("views/Dashboard.fxml", "Chats");
  }

  @FXML
  private void onContactsClick() {
    NavigationManager.getInstance().navigateTo("views/Contacts.fxml", "Contacts");
  }

  @FXML
  private void onLogoutClick() {
    // Logout via AuthService
    authService.logout();
    NavigationManager.getInstance().navigateTo("views/login.fxml", "Login");
  }
}
