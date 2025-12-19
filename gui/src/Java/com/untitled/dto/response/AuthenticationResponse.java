package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.util.Map;

/**
 * Response DTO for authentication (login/register).
 * Contains the JWT token.
 */
public record AuthenticationResponse(
    String token) {
  /**
   * Creates an AuthenticationResponse from a JSON string.
   */
  public static AuthenticationResponse fromJson(String json) {
    Map<String, Object> map = JsonMapper.parseJson(json);
    String token = JsonMapper.getString(map, "token");
    return new AuthenticationResponse(token);
  }
}
