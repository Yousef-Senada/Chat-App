package com.untitled.api.endpoints;

import com.untitled.api.ApiClient;
import com.untitled.dto.request.LoginRequest;
import com.untitled.dto.request.RegisterRequest;
import com.untitled.dto.response.AuthenticationResponse;

import java.util.concurrent.CompletableFuture;

public class AuthApi {

  private final ApiClient client;

  public AuthApi(ApiClient client) {
    this.client = client;
  }

  public CompletableFuture<AuthenticationResponse> login(String username, String password) {
    LoginRequest request = new LoginRequest(username, password);
    return client.post("/api/auth/login", request)
        .thenApply(AuthenticationResponse::fromJson);
  }

  public CompletableFuture<AuthenticationResponse> register(
      String name, String username, String phoneNumber, String password) {
    RegisterRequest request = new RegisterRequest(name, username, phoneNumber, password);
    return client.post("/api/auth/register", request)
        .thenApply(AuthenticationResponse::fromJson);
  }
}
