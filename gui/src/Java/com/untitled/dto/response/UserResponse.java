package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.util.Map;
import java.util.UUID;

public record UserResponse(
    UUID ID,
    String name,
    String username,
    String phoneNumber) {
  public static UserResponse fromJson(String json) {
    Map<String, Object> map = JsonMapper.parseJson(json);
    return fromMap(map);
  }

  public static UserResponse fromMap(Map<String, Object> map) {
    UUID id = JsonMapper.getUUID(map, "ID");
    if (id == null) {
      id = JsonMapper.getUUID(map, "id");
    }
    String name = JsonMapper.getString(map, "name");
    String username = JsonMapper.getString(map, "username");
    String phoneNumber = JsonMapper.getString(map, "phoneNumber");

    return new UserResponse(id, name, username, phoneNumber);
  }

  public String getInitials() {
    if (name == null || name.isEmpty())
      return "?";
    String[] parts = name.split(" ");
    if (parts.length >= 2) {
      return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }
    return ("" + name.charAt(0)).toUpperCase();
  }
}
