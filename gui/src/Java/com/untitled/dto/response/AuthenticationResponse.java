package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.util.Map;

public record AuthenticationResponse(
    String token) {
  public static AuthenticationResponse fromJson(String json) {
    Map<String, Object> map = JsonMapper.parseJson(json);
    String token = JsonMapper.getString(map, "token");
    return new AuthenticationResponse(token);
  }
}
