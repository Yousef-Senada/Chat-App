package com.untitled.controllers;

import com.untitled.dto.response.ContactDisplayResponse;
import com.untitled.service.ChatService;
import com.untitled.service.ContactService;
import com.untitled.service.ServiceLocator;
import com.untitled.store.ChatStore;
import com.untitled.store.ContactStore;
import com.untitled.NavigationManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateController {

  @FXML
  private Button cancelBtn;
  @FXML
  private Button createGroupBtn;
  @FXML
  private TextField groupNameField;
  @FXML
  private TextArea groupDescField;
  @FXML
  private TextField categoryField;
  @FXML
  private VBox membersContainer;
  @FXML
  private ProgressIndicator loadingIndicator;
  @FXML
  private Label errorLabel;

  private ChatService chatService;
  private ContactService contactService;
  private ChatStore chatStore;
  private ContactStore contactStore;

  private final ObservableList<UUID> selectedMembers = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    System.out.println("Create Group View Initialized");

    chatService = ServiceLocator.getInstance().getChatService();
    contactService = ServiceLocator.getInstance().getContactService();
    chatStore = chatService.getStore();
    contactStore = contactService.getStore();

    if (loadingIndicator != null) {
      loadingIndicator.visibleProperty().bind(chatStore.loadingProperty());
      loadingIndicator.managedProperty().bind(chatStore.loadingProperty());
    }

    if (errorLabel != null) {
      errorLabel.textProperty().bind(chatStore.errorProperty());
      errorLabel.visibleProperty().bind(chatStore.errorProperty().isNotEmpty());
      errorLabel.managedProperty().bind(chatStore.errorProperty().isNotEmpty());
    }

    contactService.loadContacts();

    contactStore.getContacts().addListener((javafx.collections.ListChangeListener<ContactDisplayResponse>) change -> {
      loadMembersSelection();
    });

    loadMembersSelection();
  }

  private void loadMembersSelection() {
    if (membersContainer == null)
      return;

    membersContainer.getChildren().clear();

    if (contactStore.getContacts().isEmpty()) {
      Label emptyLabel = new Label("No contacts yet. Add contacts to create a group.");
      emptyLabel.setTextFill(Color.web("#6B7280"));
      membersContainer.getChildren().add(emptyLabel);
      return;
    }

    for (ContactDisplayResponse contact : contactStore.getContacts()) {
      HBox memberItem = createMemberSelectionItem(contact);
      membersContainer.getChildren().add(memberItem);
    }
  }

  private HBox createMemberSelectionItem(ContactDisplayResponse contact) {
    HBox container = new HBox(12);
    container.setAlignment(Pos.CENTER_LEFT);
    container.setPadding(new Insets(8, 12, 8, 12));
    container.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 8;");

    CheckBox checkBox = new CheckBox();
    checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
      if (isSelected) {
        selectedMembers.add(contact.contactUserId());
      } else {
        selectedMembers.remove(contact.contactUserId());
      }
    });

    StackPane avatarContainer = new StackPane();
    Circle avatar = new Circle(18);
    avatar.setFill(Color.web(generateAvatarColor(contact.displayName())));

    Label initials = new Label(contact.getInitials());
    initials.setTextFill(Color.WHITE);
    initials.setFont(Font.font("System", FontWeight.BOLD, 10));

    avatarContainer.getChildren().addAll(avatar, initials);

    VBox infoBox = new VBox(2);
    HBox.setHgrow(infoBox, Priority.ALWAYS);

    Label nameLabel = new Label(contact.displayName());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
    nameLabel.setTextFill(Color.web("#111827"));

    Label usernameLabel = new Label("@" + contact.username());
    usernameLabel.setFont(Font.font("System", 11));
    usernameLabel.setTextFill(Color.web("#6B7280"));

    infoBox.getChildren().addAll(nameLabel, usernameLabel);

    container.getChildren().addAll(checkBox, avatarContainer, infoBox);

    container.setOnMouseClicked(e -> checkBox.setSelected(!checkBox.isSelected()));
    container.setStyle("-fx-cursor: hand; -fx-background-color: #F9FAFB; -fx-background-radius: 8;");

    return container;
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
  private void onCancelClick() {
    NavigationManager.getInstance().navigateTo("views/Dashboard.fxml", "ChatApp - Dashboard");
  }

  @FXML
  private void onCreateGroupClick() {
    String groupName = groupNameField != null ? groupNameField.getText() : "";

    if (groupName == null || groupName.trim().isEmpty()) {
      chatStore.setError("Please enter a group name");
      return;
    }

    if (selectedMembers.isEmpty()) {
      chatStore.setError("Please select at least one member");
      return;
    }

    chatStore.clearError();

    List<UUID> memberIds = new ArrayList<>(selectedMembers);
    chatService.createGroupChat(groupName.trim(), null, memberIds);

    System.out.println("Creating group: " + groupName + " with " + memberIds.size() + " members");

    NavigationManager.getInstance().navigateTo("views/Dashboard.fxml", "ChatApp - Dashboard");
  }
}
