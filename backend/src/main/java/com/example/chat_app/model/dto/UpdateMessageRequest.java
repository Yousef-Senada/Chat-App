package com.example.chat_app.model.dto;

import java.util.UUID;

public record UpdateMessageRequest(
        UUID messageId,
        String newContent
) {}
