package com.example.chat_app.model.dto;

import java.util.List;
import java.util.UUID;

public record MemberUpdateDto(
    UUID chatId,
    List<MemberDisplayDto> updatedMembers,
    String updateType
) {}