package com.untitled.dto.request;

import java.util.List;
import java.util.UUID;

public record UpdateMembershipRequest(
    UUID chatId,
    List<UUID> memberUserIds) {
}
