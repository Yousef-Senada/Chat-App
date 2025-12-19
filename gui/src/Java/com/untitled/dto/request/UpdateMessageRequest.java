package com.untitled.dto.request;

import java.util.UUID;

/**
 * Request DTO for editing a message.
 * PATCH /api/messages
 */
public record UpdateMessageRequest(
    UUID messageId,
    String newContent) {
}
