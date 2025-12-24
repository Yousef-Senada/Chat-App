package com.untitled.controllers;

import com.untitled.dto.response.ContactDisplayResponse;
import com.untitled.service.ChatService;
import com.untitled.service.ContactService;
import com.untitled.service.ServiceLocator;
import com.untitled.store.ContactStore;
import com.untitled.NavigationManager;

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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ContactsController {

  @FXML
  private VBox contactListContainer;
  @FXML
  private VBox emptyDetailsState;
  @FXML
  private VBox contactDetailsView;
  @FXML
  private TextField searchField;
  @FXML
  private Label contactCountLabel;
  @FXML
  private ProgressIndicator loadingIndicator;
  @FXML
  private Label errorLabel;

  @FXML
  private Circle contactAvatar;
  @FXML
  private Label contactNameLabel;
  @FXML
  private Label contactStatusLabel;
  @FXML
  private Label contactEmailLabel;
  @FXML
  private Label contactStatusMsgLabel;
  @FXML
  private Circle statusIndicator;

  @FXML
  private VBox addContactForm;
  @FXML
  private Button addContactBtn;
  @FXML
  private TextField phoneNumberField;
  @FXML
  private TextField displayNameField;
  @FXML
  private Label addContactErrorLabel;

  private ContactService contactService;
  private ChatService chatService;
  private ContactStore contactStore;
  private ContactDisplayResponse selectedContact;

  @FXML
  public void initialize() {
    System.out.println("Contacts View Initialized");

    contactService = ServiceLocator.getInstance().getContactService();
    chatService = ServiceLocator.getInstance().getChatService();
    contactStore = contactService.getStore();

    if (loadingIndicator != null) {
      loadingIndicator.visibleProperty().bind(contactStore.loadingProperty());
      loadingIndicator.managedProperty().bind(contactStore.loadingProperty());
    }

    if (errorLabel != null) {
      errorLabel.textProperty().bind(contactStore.errorProperty());
      errorLabel.visibleProperty().bind(contactStore.errorProperty().isNotEmpty());
      errorLabel.managedProperty().bind(contactStore.errorProperty().isNotEmpty());
    }

    contactStore.getContacts().addListener((ListChangeListener<ContactDisplayResponse>) change -> {
      updateContactList();
      updateContactCount();
    });

    setupSearchListener();

    contactService.loadContacts();
  }

  private void updateContactList() {
    contactListContainer.getChildren().clear();

    for (ContactDisplayResponse contact : contactStore.getContacts()) {
      HBox contactItem = createContactItem(contact);
      contactListContainer.getChildren().add(contactItem);
    }
  }

  private HBox createContactItem(ContactDisplayResponse contact) {
    HBox container = new HBox(12);
    container.setAlignment(Pos.CENTER_LEFT);
    container.setPadding(new Insets(12, 16, 12, 16));
    container.setStyle("-fx-cursor: hand;");
    container.getStyleClass().add("contact-item");

    container.setOnMouseEntered(e -> {
      container.setStyle("-fx-cursor: hand; -fx-background-color: #F9FAFB;");
    });
    container.setOnMouseExited(e -> {
      container.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
    });

    StackPane avatarContainer = new StackPane();
    Circle avatar = new Circle(22);
    avatar.setFill(Color.web(generateAvatarColor(contact.displayName())));

    Label initials = new Label(contact.getInitials());
    initials.setTextFill(Color.WHITE);
    initials.setFont(Font.font("System", FontWeight.BOLD, 12));

    avatarContainer.getChildren().addAll(avatar, initials);

    VBox infoBox = new VBox(2);
    HBox.setHgrow(infoBox, Priority.ALWAYS);

    Label nameLabel = new Label(contact.displayName());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
    nameLabel.setTextFill(Color.web("#111827"));
    nameLabel.getStyleClass().add("contact-name");

    Label usernameLabel = new Label("@" + contact.username());
    usernameLabel.setFont(Font.font("System", 12));
    usernameLabel.setTextFill(Color.web("#6B7280"));

    infoBox.getChildren().addAll(nameLabel, usernameLabel);

    Button deleteBtn = new Button("✕");
    deleteBtn
        .setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-font-size: 14px;");
    deleteBtn.setOnAction(e -> {
      e.consume();
      onDeleteContact(contact);
    });
    deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
        "-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-font-size: 14px; -fx-background-radius: 50;"));
    deleteBtn.setOnMouseExited(e -> deleteBtn
        .setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-font-size: 14px;"));

    container.getChildren().addAll(avatarContainer, infoBox, deleteBtn);

    container.setOnMouseClicked(e -> {
      if (e.getTarget() != deleteBtn) {
        selectContact(contact);
      }
    });

    return container;
  }

  private void setupSearchListener() {
    if (searchField != null) {
      searchField.textProperty().addListener((obs, oldVal, newVal) -> {
        filterContacts(newVal);
      });
    }
  }

  private void filterContacts(String query) {
    contactListContainer.getChildren().clear();
    int count = 0;

    for (ContactDisplayResponse contact : contactStore.getContacts()) {
      if (query == null || query.isEmpty() ||
          contact.displayName().toLowerCase().contains(query.toLowerCase()) ||
          contact.username().toLowerCase().contains(query.toLowerCase())) {
        HBox contactItem = createContactItem(contact);
        contactListContainer.getChildren().add(contactItem);
        count++;
      }
    }

    contactCountLabel.setText(count + " contact" + (count != 1 ? "s" : ""));
  }

  private void updateContactCount() {
    int count = contactStore.getContacts().size();
    contactCountLabel.setText(count + " contact" + (count != 1 ? "s" : ""));
  }

  private void selectContact(ContactDisplayResponse contact) {
    this.selectedContact = contact;

    emptyDetailsState.setVisible(false);
    emptyDetailsState.setManaged(false);
    contactDetailsView.setVisible(true);
    contactDetailsView.setManaged(true);

    contactAvatar.setFill(Color.web(generateAvatarColor(contact.displayName())));
    contactNameLabel.setText(contact.displayName());
    contactEmailLabel.setText("@" + contact.username());
    contactStatusMsgLabel.setText(contact.phoneNumber() != null ? contact.phoneNumber() : "");

    contactStatusLabel.setText("Contact");
    contactStatusLabel.setTextFill(Color.web("#6B7280"));
    statusIndicator.setFill(Color.web("#6B7280"));
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
  private void onAddContactClick() {
    addContactForm.setVisible(true);
    addContactForm.setManaged(true);
    clearAddContactForm();
  }

  @FXML
  private void onCancelAddContact() {
    addContactForm.setVisible(false);
    addContactForm.setManaged(false);
    clearAddContactForm();
  }

  @FXML
  private void onConfirmAddContact() {
    String phoneNumber = phoneNumberField.getText().trim();
    String displayName = displayNameField.getText().trim();

    if (phoneNumber.isEmpty()) {
      showAddContactError("Please enter a phone number");
      return;
    }

    if (displayName.isEmpty()) {
      showAddContactError("Please enter a display name");
      return;
    }

    hideAddContactError();
    contactService.addContact(phoneNumber, displayName);

    addContactForm.setVisible(false);
    addContactForm.setManaged(false);
    clearAddContactForm();
  }

  private void clearAddContactForm() {
    if (phoneNumberField != null)
      phoneNumberField.clear();
    if (displayNameField != null)
      displayNameField.clear();
    hideAddContactError();
  }

  private void showAddContactError(String message) {
    if (addContactErrorLabel != null) {
      addContactErrorLabel.setText(message);
      addContactErrorLabel.setVisible(true);
      addContactErrorLabel.setManaged(true);
    }
  }

  private void hideAddContactError() {
    if (addContactErrorLabel != null) {
      addContactErrorLabel.setVisible(false);
      addContactErrorLabel.setManaged(false);
    }
  }

  private void onDeleteContact(ContactDisplayResponse contact) {
    if (contact != null && contact.contactUserId() != null) {
      contactService.deleteContact(contact.contactUserId());
    }
  }

  @FXML
  private void onStartChatClick() {
    if (selectedContact != null) {
      chatService.createP2PChat(selectedContact.contactUserId());
      NavigationManager.getInstance().navigateTo("views/Dashboard.fxml", "Chats");
    }
  }

  @FXML
  private void onChatsClick() {
    NavigationManager.getInstance().navigateTo("views/Dashboard.fxml", "Chats");
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
