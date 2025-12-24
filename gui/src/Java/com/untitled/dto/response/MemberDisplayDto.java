package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.util.Map;
import java.util.UUID;

public record MemberDisplayDto(
    UUID userId,
    String username,
    String role) {
  public static MemberDisplayDto fromMap(Map<String, Object> map) {
    UUID userId = JsonMapper.getUUID(map, "userId");
    String username = JsonMapper.getString(map, "username");
    String role = JsonMapper.getString(map, "role");

    return new MemberDisplayDto(userId, username, role);
  }

  public String name() {
    return username;
  }

  public boolean isAdmin() {
    return "ADMIN".equals(role);
  }

  public String getInitials() {
    if (username == null || username.isEmpty())
      return "?";
    return ("" + username.charAt(0)).toUpperCase();
  }
}
