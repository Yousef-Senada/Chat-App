package com.untitled.store;

import com.untitled.dto.response.ContactDisplayResponse;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ContactStore {

  private final ObservableList<ContactDisplayResponse> contacts = FXCollections.observableArrayList();
  private final BooleanProperty loading = new SimpleBooleanProperty(false);
  private final StringProperty error = new SimpleStringProperty();

  public ObservableList<ContactDisplayResponse> getContacts() {
    return contacts;
  }

  public BooleanProperty loadingProperty() {
    return loading;
  }

  public StringProperty errorProperty() {
    return error;
  }

  public boolean isLoading() {
    return loading.get();
  }

  public String getError() {
    return error.get();
  }

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
