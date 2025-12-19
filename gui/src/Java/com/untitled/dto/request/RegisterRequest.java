package com.untitled.dto.request;

/**
 * Request DTO for user registration.
 * POST /api/auth/register
 */
public record RegisterRequest(
    String name,
    String username,
    String phoneNumber,
    String password) {
}
