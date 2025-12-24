package com.untitled.dto.request;

import java.util.UUID;

public record SendMessageRequest(
    UUID chatId,
    String messageType,
    String content,
    String mediaUrl
) {
}
