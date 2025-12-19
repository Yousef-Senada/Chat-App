package com.untitled.api.endpoints;

import com.untitled.api.ApiClient;
import com.untitled.dto.response.UserResponse;

import java.util.concurrent.CompletableFuture;

/**
 * API endpoint class for user operations.
 */
public class UsersApi {

  private final ApiClient client;

  public UsersApi(ApiClient client) {
    this.client = client;
  }

  /**
   * Gets the current user's profile.
   * GET /api/users/profile
   */
  public CompletableFuture<UserResponse> getProfile() {
    return client.get("/api/users/profile")
        .thenApply(UserResponse::fromJson);
  }
}
