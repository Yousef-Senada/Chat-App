package com.untitled.api.endpoints;

import com.untitled.api.ApiClient;
import com.untitled.dto.request.AddContactRequest;
import com.untitled.dto.response.ContactDisplayResponse;
import com.untitled.dto.response.ContactMatchResponse;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ContactsApi {

  private final ApiClient client;

  public ContactsApi(ApiClient client) {
    this.client = client;
  }

  public CompletableFuture<List<ContactDisplayResponse>> getAllContacts() {
    return client.get("/api/contacts")
        .thenApply(ContactDisplayResponse::listFromJson);
  }

  public CompletableFuture<String> addContact(String phoneNumber, String displayName) {
    AddContactRequest request = new AddContactRequest(phoneNumber, displayName);
    return client.post("/api/contacts/add", request);
  }

  public CompletableFuture<String> deleteContact(UUID contactUserId) {
    return client.delete("/api/contacts/delete/" + contactUserId);
  }

  public CompletableFuture<ContactMatchResponse> findByPhone(String phone) {
    return client.get("/api/contacts/phone?phone=" + phone)
        .thenApply(ContactMatchResponse::fromJson);
  }
}
