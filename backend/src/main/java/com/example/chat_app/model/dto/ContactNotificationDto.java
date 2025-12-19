package com.example.chat_app.model.dto;

import java.util.UUID;

public record ContactNotificationDto(
    UUID userId,
    String username,
    String updateType 
) {}
