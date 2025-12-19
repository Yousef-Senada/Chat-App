package com.example.chat_app.model.dto;

public record AddContactRequest(
        String targetPhoneNumber,
        String customDisplayName
) {}
