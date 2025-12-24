package com.untitled.dto.request;

import java.util.UUID;

public record UpdateMessageRequest(
    UUID messageId,
    String newContent) {
}
