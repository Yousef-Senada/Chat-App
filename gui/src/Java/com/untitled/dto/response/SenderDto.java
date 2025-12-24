package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.util.Map;
import java.util.UUID;

public record SenderDto(
    UUID senderId,
    String username) {
  public static SenderDto fromMap(Map<String, Object> map) {
    UUID senderId = JsonMapper.getUUID(map, "senderId");
    String username = JsonMapper.getString(map, "username");

    return new SenderDto(senderId, username);
  }

  public String name() {
    return username;
  }

  public String getInitials() {
    if (username == null || username.isEmpty())
      return "?";
    return ("" + username.charAt(0)).toUpperCase();
  }
}
