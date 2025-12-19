package com.untitled.dto.request;

/**
 * Request DTO for adding a contact.
 * POST /api/contacts/add
 */
public record AddContactRequest(
    String targetPhoneNumber,
    String customDisplayName) {
}
