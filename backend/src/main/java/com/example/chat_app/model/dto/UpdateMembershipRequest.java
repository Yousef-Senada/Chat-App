package com.example.chat_app.model.dto;

import java.util.List;
import java.util.UUID;

public record UpdateMembershipRequest(
    UUID chatId,
    List<UUID> memberUserIds
) {}
