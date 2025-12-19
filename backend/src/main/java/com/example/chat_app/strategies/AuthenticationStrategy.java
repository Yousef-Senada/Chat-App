package com.example.chat_app.strategies;

import com.example.chat_app.model.dto.AuthenticationResponse;
import com.example.chat_app.model.dto.LoginRequest;

public interface AuthenticationStrategy {
    AuthenticationResponse authenticate(LoginRequest request);

    boolean supports(String authType);
}
