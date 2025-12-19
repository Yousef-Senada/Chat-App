package com.example.chat_app.model.dto;

import java.util.UUID;

public record MemberDisplayDto(
        UUID userId,
        String username,
        String role
) {}
