package com.example.chat_app.model.dto;

import java.util.UUID;

public record UpdateMemberRoleRequest(
        UUID chatId,
        UUID targetUserId,
        String newRole
) {}
