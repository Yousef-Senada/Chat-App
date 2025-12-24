package com.untitled.service;

import com.untitled.api.endpoints.ContactsApi;
import com.untitled.dto.response.ContactMatchResponse;
import com.untitled.store.ContactStore;
import com.untitled.util.ErrorHandler;

import javafx.application.Platform;

import java.util.UUID;

public class ContactService {

  private final ContactsApi contactsApi;
  private final ContactStore contactStore;

  public ContactService(ContactsApi contactsApi, ContactStore contactStore) {
    this.contactsApi = contactsApi;
    this.contactStore = contactStore;
  }

  public void loadContacts() {
    contactStore.setLoading(true);
    contactStore.clearError();

    contactsApi.getAllContacts()
        .thenAccept(contacts -> {
          Platform.runLater(() -> {
            contactStore.setContacts(contacts);
            contactStore.setLoading(false);
            System.out.println("Loaded " + contacts.size() + " contacts");
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            contactStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            contactStore.setError(errorMessage);
            System.out.println("Failed to load contacts: " + errorMessage);
          });
          return null;
        });
  }

  public void addContact(String phoneNumber, String displayName) {
    contactStore.setLoading(true);
    contactStore.clearError();

    contactsApi.addContact(phoneNumber, displayName)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            contactStore.setLoading(false);
            loadContacts();
            System.out.println("Contact added: " + response);
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            contactStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            contactStore.setError(errorMessage);
          });
          return null;
        });
  }

  public void deleteContact(UUID contactUserId) {
    contactStore.setLoading(true);

    contactsApi.deleteContact(contactUserId)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            contactStore.setLoading(false);
            loadContacts();
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            contactStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            contactStore.setError(errorMessage);
          });
          return null;
        });
  }

  public void findByPhone(String phone, java.util.function.Consumer<ContactMatchResponse> onSuccess) {
    contactsApi.findByPhone(phone)
        .thenAccept(contact -> {
          Platform.runLater(() -> {
            if (onSuccess != null) {
              onSuccess.accept(contact);
            }
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            String errorMessage = extractErrorMessage(throwable);
            contactStore.setError(errorMessage);
          });
          return null;
        });
  }

  public ContactStore getStore() {
    return contactStore;
  }

  private String extractErrorMessage(Throwable throwable) {
    return ErrorHandler.extractMessage(throwable);
  }
}
