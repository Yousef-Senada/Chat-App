package com.untitled.controllers;

import com.untitled.dto.response.ChatDisplayDto;
import com.untitled.dto.response.MemberDisplayDto;
import com.untitled.dto.response.MessageDisplayDto;
import com.untitled.dto.response.UserResponse;
import com.untitled.service.ChatService;
import com.untitled.service.MessageService;
import com.untitled.service.ServiceLocator;
import com.untitled.store.AuthStore;
import com.untitled.store.ChatStore;
import com.untitled.store.MessageStore;
import com.untitled.NavigationManager;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Controller for the chat view.
 * Displays messages and allows sending new messages.
 */
public class ChatController {

  @FXML
  private VBox messagesContainer;
  @FXML
  private ScrollPane messagesScrollPane;
  @FXML
  private TextField messageInput;
  @FXML
  private Button sendButton;
  @FXML
  private HBox typingIndicator;
  @FXML
  private Label typingLabel;
  @FXML
  private ProgressIndicator loadingIndicator;

  @FXML
  private Circle chatAvatar;
  @FXML
  private Label avatarInitials;
  @FXML
  private Circle onlineIndicator;
  @FXML
  private Label chatNameLabel;
  @FXML
  private Label chatStatusLabel;

  @FXML
  private Circle profileAvatar;
  @FXML
  private Label profileNameLabel;

  @FXML
  private VBox attachmentPopup;
  @FXML
  private VBox attachmentButton;

  // Group Members UI Fields
  @FXML
  private VBox groupMembersSection;
  @FXML
  private Label memberCountLabel;
  @FXML
  private VBox membersListContainer;
  @FXML
  private VBox addMemberForm;
  @FXML
  private Button addMemberBtn;
  @FXML
  private TextField memberIdField;
  @FXML
  private Button leaveChatBtn;

  private ChatService chatService;
  private MessageService messageService;
  private ChatStore chatStore;
  private MessageStore messageStore;
  private AuthStore authStore;

  private ChatDisplayDto currentChat;
  private UUID currentUserId;
  private boolean isAttachmentPopupVisible = false;

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  @FXML
  public void initialize() {
    System.out.println("Chat View Initialized");

    // Get services and stores
    chatService = ServiceLocator.getInstance().getChatService();
    messageService = ServiceLocator.getInstance().getMessageService();
    chatStore = chatService.getStore();
    messageStore = messageService.getStore();
    authStore = ServiceLocator.getInstance().getAuthStore();

    // Get current user ID
    UserResponse currentUser = authStore.getCurrentUser();
    if (currentUser != null) {
      currentUserId = currentUser.ID();
    }

    // Get active chat
    currentChat = chatStore.getActiveChat();

    // Bind loading indicator
    if (loadingIndicator != null) {
      loadingIndicator.visibleProperty().bind(messageStore.loadingProperty());
      loadingIndicator.managedProperty().bind(messageStore.loadingProperty());
    }

    // Listen to message list changes
    messageStore.getMessages().addListener((ListChangeListener<MessageDisplayDto>) change -> {
      updateMessageList();
    });

    // Listen to chat members changes
    chatStore.getActiveChatMembers().addListener((ListChangeListener<MemberDisplayDto>) change -> {
      updateMembersList();
    });

    if (currentChat != null) {
      loadChatData();
      // Load messages from API
      messageService.loadMessages(currentChat.chatId());

      // If it's a group, load members and show the section
      if (currentChat.isGroup()) {
        showGroupMembersSection();
        chatService.loadChatMembers(currentChat.chatId());
      }
    } else {
      setDefaultChatView();
    }

    messageInput.setOnAction(e -> onSendMessage());
    messageInput.setOnMouseClicked(e -> hideAttachmentPopup());
  }

  private void loadChatData() {
    if (currentChat == null)
      return;

    chatNameLabel.setText(currentChat.getDisplayName());
    chatAvatar.setFill(Color.web(generateAvatarColor(currentChat.getDisplayName())));
    avatarInitials.setText(getInitials(currentChat.getDisplayName()));

    profileNameLabel.setText(currentChat.getDisplayName());
    profileAvatar.setFill(Color.web(generateAvatarColor(currentChat.getDisplayName())));

    if (currentChat.isGroup()) {
      chatStatusLabel.setText(currentChat.members() != null ? currentChat.members().size() + " members" : "Group");
      chatStatusLabel.setTextFill(Color.web("#6B7280"));
      onlineIndicator.setVisible(false);
    } else {
      chatStatusLabel.setText("Active");
      chatStatusLabel.setTextFill(Color.web("#22C55E"));
      onlineIndicator.setFill(Color.web("#22C55E"));
    }
  }

  private void setDefaultChatView() {
    chatNameLabel.setText("Select a chat");
    chatStatusLabel.setText("");
    avatarInitials.setText("?");
    chatAvatar.setFill(Color.web("#6B7280"));
    profileNameLabel.setText("No chat selected");
  }

  private void showGroupMembersSection() {
    if (groupMembersSection != null) {
      groupMembersSection.setVisible(true);
      groupMembersSection.setManaged(true);
    }
  }

  private void hideGroupMembersSection() {
    if (groupMembersSection != null) {
      groupMembersSection.setVisible(false);
      groupMembersSection.setManaged(false);
    }
  }

  private void updateMembersList() {
    if (membersListContainer == null)
      return;

    membersListContainer.getChildren().clear();

    int count = chatStore.getActiveChatMembers().size();
    if (memberCountLabel != null) {
      memberCountLabel.setText(count + " member" + (count != 1 ? "s" : ""));
    }

    for (MemberDisplayDto member : chatStore.getActiveChatMembers()) {
      HBox memberItem = createMemberItem(member);
      membersListContainer.getChildren().add(memberItem);
    }
  }

  private HBox createMemberItem(MemberDisplayDto member) {
    HBox container = new HBox(10);
    container.setAlignment(Pos.CENTER_LEFT);
    container.setPadding(new Insets(8, 10, 8, 10));
    container.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 8;");

    // Avatar
    StackPane avatarContainer = new StackPane();
    Circle avatar = new Circle(16);
    avatar.setFill(Color.web(generateAvatarColor(member.name())));

    Label initials = new Label(member.getInitials());
    initials.setTextFill(Color.WHITE);
    initials.setFont(Font.font("System", FontWeight.BOLD, 9));

    avatarContainer.getChildren().addAll(avatar, initials);

    // Member info
    VBox infoBox = new VBox(1);
    HBox.setHgrow(infoBox, Priority.ALWAYS);

    Label nameLabel = new Label(member.name());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
    nameLabel.setTextFill(Color.web("#111827"));

    HBox badgeRow = new HBox(4);
    if (member.isAdmin()) {
      Label adminBadge = new Label("Admin");
      adminBadge.setFont(Font.font("System", 9));
      adminBadge.setTextFill(Color.web("#8B5CF6"));
      adminBadge.setStyle("-fx-background-color: #F3E8FF; -fx-padding: 2 6; -fx-background-radius: 4;");
      badgeRow.getChildren().add(adminBadge);
    }

    infoBox.getChildren().add(nameLabel);
    if (!badgeRow.getChildren().isEmpty()) {
      infoBox.getChildren().add(badgeRow);
    }

    container.getChildren().addAll(avatarContainer, infoBox);

    // Remove button (don't show for current user or admins if not admin)
    boolean isCurrentUser = member.userId() != null && member.userId().equals(currentUserId);
    if (!isCurrentUser) {
      Button removeBtn = new Button("✕");
      removeBtn.setStyle(
          "-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-font-size: 12px;");
      removeBtn.setOnAction(e -> onRemoveMember(member));
      removeBtn.setOnMouseEntered(e -> removeBtn.setStyle(
          "-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-font-size: 12px; -fx-background-radius: 50;"));
      removeBtn.setOnMouseExited(e -> removeBtn.setStyle(
          "-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-font-size: 12px;"));
      container.getChildren().add(removeBtn);
    }

    return container;
  }

  private void onRemoveMember(MemberDisplayDto member) {
    if (currentChat != null && member.userId() != null) {
      System.out.println("Removing member: " + member.name() + " (ID: " + member.userId() + ")");
      chatService.removeMember(currentChat.chatId(), member.userId());
    }
  }

  // ===== Add Member Form Handlers =====

  @FXML
  private void onAddMemberClick() {
    if (addMemberForm != null) {
      addMemberForm.setVisible(true);
      addMemberForm.setManaged(true);
    }
    if (addMemberBtn != null) {
      addMemberBtn.setVisible(false);
      addMemberBtn.setManaged(false);
    }
    if (memberIdField != null) {
      memberIdField.clear();
    }
  }

  @FXML
  private void onCancelAddMember() {
    if (addMemberForm != null) {
      addMemberForm.setVisible(false);
      addMemberForm.setManaged(false);
    }
    if (addMemberBtn != null) {
      addMemberBtn.setVisible(true);
      addMemberBtn.setManaged(true);
    }
    if (memberIdField != null) {
      memberIdField.clear();
    }
  }

  @FXML
  private void onConfirmAddMember() {
    if (memberIdField == null || currentChat == null)
      return;

    String memberIdStr = memberIdField.getText().trim();
    if (memberIdStr.isEmpty()) {
      System.out.println("Please enter a user ID");
      return;
    }

    try {
      UUID memberId = UUID.fromString(memberIdStr);
      System.out.println("Adding member with ID: " + memberId);
      chatService.addMember(currentChat.chatId(), memberId);

      // Hide the form
      onCancelAddMember();
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid UUID format: " + memberIdStr);
    }
  }

  @FXML
  private void onLeaveGroupClick() {
    if (currentChat != null) {
      System.out.println("Leaving group: " + currentChat.getDisplayName());
      chatService.leaveChat(currentChat.chatId());
      NavigationManager.getInstance().navigateTo("views/Dashboard.fxml", "Chats");
    }
  }

  private void updateMessageList() {
    messagesContainer.getChildren().clear();

    for (MessageDisplayDto message : messageStore.getMessages()) {
      HBox messageComponent = createMessageComponent(message);
      messagesContainer.getChildren().add(messageComponent);
    }

    Platform.runLater(() -> {
      messagesScrollPane.setVvalue(1.0);
    });
  }

  private HBox createMessageComponent(MessageDisplayDto message) {
    boolean isOutgoing = message.sender() != null &&
        message.sender().senderId() != null &&
        message.sender().senderId().equals(currentUserId);

    HBox container = new HBox();
    container.setPadding(new Insets(4, 16, 4, 16));
    container.setAlignment(isOutgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

    VBox messageBox = new VBox(4);
    messageBox.setMaxWidth(350);
    messageBox.setPadding(new Insets(10, 14, 10, 14));

    if (isOutgoing) {
      messageBox.setStyle("-fx-background-color: #8B5CF6; -fx-background-radius: 16 16 4 16;");
    } else {
      messageBox.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 16 16 16 4;");
    }

    // Sender name (for incoming messages in groups)
    if (!isOutgoing && currentChat != null && currentChat.isGroup() && message.sender() != null) {
      Label senderLabel = new Label(message.sender().name());
      senderLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
      senderLabel.setTextFill(Color.web("#8B5CF6"));
      messageBox.getChildren().add(senderLabel);
    }

    // Message content
    Label contentLabel = new Label(message.getDisplayContent());
    contentLabel.setWrapText(true);
    contentLabel.setFont(Font.font("System", 14));
    contentLabel.setTextFill(isOutgoing ? Color.WHITE : Color.web("#111827"));
    messageBox.getChildren().add(contentLabel);

    // Timestamp
    HBox timeRow = new HBox(4);
    timeRow.setAlignment(isOutgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

    String timeText = message.timestamp() != null ? message.timestamp().format(TIME_FORMATTER) : "";
    if (message.isEdited()) {
      timeText = "edited · " + timeText;
    }

    Label timeLabel = new Label(timeText);
    timeLabel.setFont(Font.font("System", 10));
    timeLabel.setTextFill(isOutgoing ? Color.web("#E0D4FF") : Color.web("#9CA3AF"));
    timeRow.getChildren().add(timeLabel);

    messageBox.getChildren().add(timeRow);

    // Avatar for incoming messages
    if (!isOutgoing && message.sender() != null) {
      Circle avatar = new Circle(16);
      avatar.setFill(Color.web(generateAvatarColor(message.sender().name())));

      Label initials = new Label(message.sender().getInitials());
      initials.setTextFill(Color.WHITE);
      initials.setFont(Font.font("System", FontWeight.BOLD, 10));

      StackPane avatarContainer = new StackPane(avatar, initials);
      HBox.setMargin(avatarContainer, new Insets(0, 8, 0, 0));

      container.getChildren().addAll(avatarContainer, messageBox);
    } else {
      container.getChildren().add(messageBox);
    }

    return container;
  }

  @FXML
  private void onSendMessage() {
    String content = messageInput.getText();
    if (content == null || content.trim().isEmpty())
      return;
    if (currentChat == null)
      return;

    messageService.sendTextMessage(currentChat.chatId(), content.trim());

    messageInput.clear();
    messageInput.requestFocus();
  }

  @FXML
  private void onAttachmentClick(MouseEvent event) {
    isAttachmentPopupVisible = !isAttachmentPopupVisible;
    attachmentPopup.setVisible(isAttachmentPopupVisible);
    attachmentPopup.setManaged(isAttachmentPopupVisible);
    event.consume();
  }

  private void hideAttachmentPopup() {
    isAttachmentPopupVisible = false;
    attachmentPopup.setVisible(false);
    attachmentPopup.setManaged(false);
  }

  @FXML
  private void onSendImage(MouseEvent event) {
    hideAttachmentPopup();
    event.consume();

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));

    Stage stage = (Stage) messageInput.getScene().getWindow();
    File selectedFile = fileChooser.showOpenDialog(stage);

    if (selectedFile != null && currentChat != null) {
      String filePath = selectedFile.toURI().toString();
      String fileName = selectedFile.getName();

      // Note: File upload not implemented - would need a file upload endpoint
      messageService.sendImageMessage(currentChat.chatId(), fileName, filePath);
    }
  }

  @FXML
  private void onSendVideo(MouseEvent event) {
    hideAttachmentPopup();
    event.consume();
    // Video upload not implemented in API
    System.out.println("Video upload not implemented");
  }

  @FXML
  private void onSendFile(MouseEvent event) {
    hideAttachmentPopup();
    event.consume();
    // File upload not implemented in API
    System.out.println("File upload not implemented");
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

  @FXML
  private void onBackClick() {
    messageService.clearMessages();
    chatService.setActiveChat(null);
    NavigationManager.getInstance().navigateTo("views/Dashboard.fxml", "Chats");
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
    ServiceLocator.reset();
    NavigationManager.getInstance().navigateTo("views/login.fxml", "Login");
  }
}
