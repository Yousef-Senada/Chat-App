package com.untitled.dto.request;

public record RegisterRequest(
    String name,
    String username,
    String phoneNumber,
    String password) {
}
