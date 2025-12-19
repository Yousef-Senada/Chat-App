package com.untitled.dto.request;

/**
 * Request DTO for user login.
 * POST /api/auth/login
 */
public record LoginRequest(
    String username,
    String password) {
}
