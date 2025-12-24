package com.untitled.dto.request;

public record AddContactRequest(
    String targetPhoneNumber,
    String customDisplayName) {
}
