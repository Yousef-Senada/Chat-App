package com.example.chat_app.model.dto;

import java.util.UUID;

public record SendMessageRequest(
        UUID chatId,
        String messageType,
        String content,
        String mediaUrl
) {}
