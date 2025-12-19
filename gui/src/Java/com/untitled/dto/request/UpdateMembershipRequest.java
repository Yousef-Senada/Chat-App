package com.untitled.dto.request;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for updating chat membership (adding/removing members).
 * POST /api/chats/members (add)
 * DELETE /api/chats/members (remove)
 */
public record UpdateMembershipRequest(
    UUID chatId,
    List<UUID> memberUserIds) {
}
