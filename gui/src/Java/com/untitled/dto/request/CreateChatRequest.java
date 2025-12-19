package com.untitled.dto.request;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new chat.
 * POST /api/chats
 */
public record CreateChatRequest(
    String chatType, // "P2P" or "GROUP"
    String groupName, // Required for GROUP chats
    String groupImage, // Optional URL for group image
    List<UUID> membersId // List of user IDs to add to the chat
) {
}
