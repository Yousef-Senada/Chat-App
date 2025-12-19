package com.untitled.store;

import com.untitled.dto.response.ContactDisplayResponse;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Observable store for contact state.
 * Uses JavaFX properties for UI binding.
 */
public class ContactStore {

  private final ObservableList<ContactDisplayResponse> contacts = FXCollections.observableArrayList();
  private final BooleanProperty loading = new SimpleBooleanProperty(false);
  private final StringProperty error = new SimpleStringProperty();

  // Observable list getter
  public ObservableList<ContactDisplayResponse> getContacts() {
    return contacts;
  }

  // Property getters for binding
  public BooleanProperty loadingProperty() {
    return loading;
  }

  public StringProperty errorProperty() {
    return error;
  }

  // Value getters
  public boolean isLoading() {
    return loading.get();
  }

  public String getError() {
    return error.get();
  }

  // Actions
  public void setContacts(List<ContactDisplayResponse> newContacts) {
    contacts.clear();
    contacts.addAll(newContacts);
  }

  public void addContact(ContactDisplayResponse contact) {
    contacts.add(contact);
  }

  public void removeContact(ContactDisplayResponse contact) {
    contacts.remove(contact);
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

  public void clear() {
    contacts.clear();
    error.set(null);
  }
}
