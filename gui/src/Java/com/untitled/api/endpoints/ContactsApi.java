package com.untitled.api.endpoints;

import com.untitled.api.ApiClient;
import com.untitled.dto.request.AddContactRequest;
import com.untitled.dto.response.ContactDisplayResponse;
import com.untitled.dto.response.ContactMatchResponse;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API endpoint class for contact operations.
 */
public class ContactsApi {

  private final ApiClient client;

  public ContactsApi(ApiClient client) {
    this.client = client;
  }

  /**
   * Gets all contacts for the current user.
   * GET /api/contacts
   */
  public CompletableFuture<List<ContactDisplayResponse>> getAllContacts() {
    return client.get("/api/contacts")
        .thenApply(ContactDisplayResponse::listFromJson);
  }

  /**
   * Adds a new contact.
   * POST /api/contacts/add
   */
  public CompletableFuture<String> addContact(String phoneNumber, String displayName) {
    AddContactRequest request = new AddContactRequest(phoneNumber, displayName);
    return client.post("/api/contacts/add", request);
  }

  /**
   * Deletes a contact.
   * DELETE /api/contacts/delete/{contactUserId}
   */
  public CompletableFuture<String> deleteContact(UUID contactUserId) {
    return client.delete("/api/contacts/delete/" + contactUserId);
  }

  /**
   * Finds a user by phone number.
   * GET /api/contacts/phone?phone={phone}
   */
  public CompletableFuture<ContactMatchResponse> findByPhone(String phone) {
    return client.get("/api/contacts/phone?phone=" + phone)
        .thenApply(ContactMatchResponse::fromJson);
  }
}
