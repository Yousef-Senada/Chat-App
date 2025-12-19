package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for chat member data.
 */
public record MemberDisplayDto(
    UUID userId,
    String username,
    String role) {
  /**
   * Creates a MemberDisplayDto from a parsed JSON map.
   */
  public static MemberDisplayDto fromMap(Map<String, Object> map) {
    UUID userId = JsonMapper.getUUID(map, "userId");
    String username = JsonMapper.getString(map, "username");
    String role = JsonMapper.getString(map, "role");

    return new MemberDisplayDto(userId, username, role);
  }

  /**
   * Gets the display name for this member.
   * Since backend doesn't provide name, we use username.
   */
  public String name() {
    return username;
  }

  /**
   * Checks if this member is an admin.
   */
  public boolean isAdmin() {
    return "ADMIN".equals(role);
  }

  /**
   * Gets initials from the member's username for avatar display.
   */
  public String getInitials() {
    if (username == null || username.isEmpty())
      return "?";
    return ("" + username.charAt(0)).toUpperCase();
  }
}
