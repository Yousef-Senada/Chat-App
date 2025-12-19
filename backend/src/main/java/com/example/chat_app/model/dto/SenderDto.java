package com.example.chat_app.model.dto;

import java.util.UUID;

public record SenderDto(
        UUID senderId,
        String username
) {}
