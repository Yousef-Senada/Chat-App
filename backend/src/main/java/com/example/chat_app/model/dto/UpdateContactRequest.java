package com.example.chat_app.model.dto;

import java.util.UUID;

public record UpdateContactRequest(
        UUID targetUserId,
        String newDisplayName,
        String newPhoneNumber
) {}
