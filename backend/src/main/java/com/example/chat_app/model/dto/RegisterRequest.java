package com.example.chat_app.model.dto;

public record RegisterRequest(
        String name,
        String username,
        String phoneNumber,
        String password
) {}
