package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for message sender data.
 */
public record SenderDto(
    UUID senderId,
    String username) {
  /**
   * Creates a SenderDto from a parsed JSON map.
   */
  public static SenderDto fromMap(Map<String, Object> map) {
    UUID senderId = JsonMapper.getUUID(map, "senderId");
    String username = JsonMapper.getString(map, "username");

    return new SenderDto(senderId, username);
  }

  /**
   * Gets the display name for this sender.
   * Since backend doesn't provide name, we use username.
   */
  public String name() {
    return username;
  }

  /**
   * Gets initials from the sender's username for avatar display.
   */
  public String getInitials() {
    if (username == null || username.isEmpty())
      return "?";
    return ("" + username.charAt(0)).toUpperCase();
  }
}
