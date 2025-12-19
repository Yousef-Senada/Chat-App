package com.untitled.dto.request;

import java.util.UUID;

/**
 * Request DTO for sending a message.
 * POST /api/messages
 */
public record SendMessageRequest(
    UUID chatId,
    String messageType, // "TEXT", "IMAGE", "AUDIO"
    String content, // Text content or description
    String mediaUrl // URL for media files (IMAGE, AUDIO)
) {
}
