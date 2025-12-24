package com.untitled.controllers;

import com.untitled.dto.response.ChatDisplayDto;
import com.untitled.service.ChatService;
import com.untitled.service.ServiceLocator;
import com.untitled.store.ChatStore;
import com.untitled.NavigationManager;

import java.util.UUID;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DashboardController {

  @FXML
  private VBox sidebar;
  @FXML
  private VBox chatListContainer;
  @FXML
  private VBox welcomePanel;
  @FXML
  private TextField searchField;
  @FXML
  private Button newChatBtn;
  @FXML
  private VBox chatNavIcon;
  @FXML
  private VBox contactsNavIcon;
  @FXML
  private VBox settingsNavIcon;
  @FXML
  private ProgressIndicator loadingIndicator;
  @FXML
  private Label errorLabel;

  private ChatService chatService;
  private ChatStore chatStore;

  @FXML
  public void initialize() {
    System.out.println("Dashboard View Initialized");

    chatService = ServiceLocator.getInstance().getChatService();
    chatStore = chatService.getStore();

    if (loadingIndicator != null) {
      loadingIndicator.visibleProperty().bind(chatStore.loadingProperty());
      loadingIndicator.managedProperty().bind(chatStore.loadingProperty());
    }

    if (errorLabel != null) {
      errorLabel.textProperty().bind(chatStore.errorProperty());
      errorLabel.visibleProperty().bind(chatStore.errorProperty().isNotEmpty());
      errorLabel.managedProperty().bind(chatStore.errorProperty().isNotEmpty());
    }

    chatStore.getChats().addListener((ListChangeListener<ChatDisplayDto>) change -> {
      updateChatList();
    });

    setupSearchListener();

    ServiceLocator.getInstance().getContactService().loadContacts();
    
    new Thread(() -> {
      try {
        Thread.sleep(500);
        chatService.updateContactNamesCache();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }).start();
    
    chatService.loadChats();
  }

  private void updateChatList() {
    chatListContainer.getChildren().clear();

    for (ChatDisplayDto chat : chatStore.getChats()) {
      HBox chatItem = createChatListItem(chat);
      chatListContainer.getChildren().add(chatItem);
    }
  }

  private HBox createChatListItem(ChatDisplayDto chat) {
    HBox container = new HBox(12);
    container.setAlignment(Pos.CENTER_LEFT);
    container.setPadding(new Insets(12, 16, 12, 16));
    container.setStyle("-fx-cursor: hand;");
    container.getStyleClass().add("chat-list-item");

    container.setOnMouseEntered(e -> {
      if (!container.getStyleClass().contains("chat-list-item-active")) {
        container.setStyle("-fx-cursor: hand; -fx-background-color: #F9FAFB;");
      }
    });
    container.setOnMouseExited(e -> {
      if (!container.getStyleClass().contains("chat-list-item-active")) {
        container.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
      }
    });

    String displayName = getDisplayNameForChat(chat);

    StackPane avatarContainer = new StackPane();
    Circle avatar = new Circle(24);
    avatar.setFill(Color.web(generateAvatarColor(displayName)));

    Label initials = new Label(getInitials(displayName));
    initials.setTextFill(Color.WHITE);
    initials.setFont(Font.font("System", FontWeight.BOLD, 14));

    avatarContainer.getChildren().addAll(avatar, initials);

    VBox infoBox = new VBox(4);
    HBox.setHgrow(infoBox, Priority.ALWAYS);

    HBox nameRow = new HBox();
    nameRow.setAlignment(Pos.CENTER_LEFT);

    Label nameLabel = new Label(displayName);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
    nameLabel.setTextFill(Color.web("#111827"));

    if (chat.isGroup()) {
      Label groupIcon = new Label("👥 ");
      groupIcon.setFont(Font.font("System", 12));
      nameRow.getChildren().add(groupIcon);
    }
    nameRow.getChildren().add(nameLabel);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    nameRow.getChildren().add(spacer);

    if (chat.isGroup() && chat.members() != null) {
      Label membersLabel = new Label(chat.members().size() + " members");
      membersLabel.setFont(Font.font("System", 11));
      membersLabel.setTextFill(Color.web("#9CA3AF"));
      nameRow.getChildren().add(membersLabel);
    }

    HBox messageRow = new HBox();
    messageRow.setAlignment(Pos.CENTER_LEFT);

    String subtitle = getSubtitle(chat);
    Label messageLabel = new Label(subtitle);
    messageLabel.setFont(Font.font("System", 13));
    messageLabel.setTextFill(Color.web("#6B7280"));
    messageLabel.setMaxWidth(200);

    messageRow.getChildren().add(messageLabel);

    infoBox.getChildren().addAll(nameRow, messageRow);
    container.getChildren().addAll(avatarContainer, infoBox);

    container.setOnMouseClicked(e -> openChat(chat));

    return container;
  }

  private String getDisplayNameForChat(ChatDisplayDto chat) {
    if (chat.isGroup()) {
      return chat.getDisplayName();
    }
    if (chat.members() != null && !chat.members().isEmpty()) {
      UUID otherUserId = chat.members().get(0).userId();
      String fallbackName = chat.members().get(0).name();
      return chatService.getDisplayNameForUser(otherUserId, fallbackName);
    }
    return chat.getDisplayName();
  }

  private String getSubtitle(ChatDisplayDto chat) {
    if (chat.isGroup()) {
      if (chat.members() != null && !chat.members().isEmpty()) {
        StringBuilder sb = new StringBuilder();
        int count = Math.min(3, chat.members().size());
        for (int i = 0; i < count; i++) {
          if (i > 0)
            sb.append(", ");
          sb.append(chat.members().get(i).name());
        }
        if (chat.members().size() > 3) {
          sb.append("...");
        }
        return sb.toString();
      }
      return "Group chat";
    }
    return "Click to open chat";
  }

  private String getInitials(String name) {
    if (name == null || name.isEmpty())
      return "?";
    String[] parts = name.split(" ");
    if (parts.length >= 2) {
      return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }
    return ("" + name.charAt(0)).toUpperCase();
  }

  private String generateAvatarColor(String name) {
    if (name == null || name.isEmpty()) {
      return "#8B5CF6";
    }
    String[] colors = { "#8B5CF6", "#3B82F6", "#22C55E", "#EF4444", "#F59E0B", "#EC4899", "#06B6D4", "#10B981" };
    int hash = Math.abs(name.hashCode());
    return colors[hash % colors.length];
  }

  private void setupSearchListener() {
    if (searchField != null) {
      searchField.textProperty().addListener((obs, oldVal, newVal) -> {
        filterChats(newVal);
      });
    }
  }

  private void filterChats(String query) {
    chatListContainer.getChildren().clear();

    for (ChatDisplayDto chat : chatStore.getChats()) {
      String displayName = getDisplayNameForChat(chat);
      if (query == null || query.isEmpty() ||
          displayName.toLowerCase().contains(query.toLowerCase())) {
        HBox chatItem = createChatListItem(chat);
        chatListContainer.getChildren().add(chatItem);
      }
    }
  }

  private void openChat(ChatDisplayDto chat) {
    chatService.setActiveChat(chat);
    NavigationManager.getInstance().navigateTo("views/Chat.fxml", "Chat - " + chat.getDisplayName());
  }

  @FXML
  private void onMenuClick() {
    System.out.println("Menu clicked");
  }

  @FXML
  private void onNewChatClick() {
    NavigationManager.getInstance().navigateTo("views/Create.fxml", "Create New Group");
  }

  @FXML
  private void onContactsClick() {
    NavigationManager.getInstance().navigateTo("views/Contacts.fxml", "Contacts");
  }

  @FXML
  private void onSettingsClick() {
    NavigationManager.getInstance().navigateTo("views/Settings.fxml", "Settings");
  }

  @FXML
  private void onLogoutClick() {
    ServiceLocator.getInstance().getWebSocketService().disconnect();
    
    ServiceLocator.reset();
    NavigationManager.getInstance().navigateTo("views/login.fxml", "Login");
  }
}
