package com.example.chat_app.model.dto;

import java.util.UUID;

public record ContactDisplayResponse(
        UUID ID,
        UUID contactUserId,
        String displayName,
        String contactUsername,
        String contactPhoneNumber
) {}
