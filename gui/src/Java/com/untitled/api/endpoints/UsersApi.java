package com.untitled.api.endpoints;

import com.untitled.api.ApiClient;
import com.untitled.dto.response.UserResponse;

import java.util.concurrent.CompletableFuture;

public class UsersApi {

  private final ApiClient client;

  public UsersApi(ApiClient client) {
    this.client = client;
  }

  public CompletableFuture<UserResponse> getProfile() {
    return client.get("/api/users/profile")
        .thenApply(UserResponse::fromJson);
  }
}
