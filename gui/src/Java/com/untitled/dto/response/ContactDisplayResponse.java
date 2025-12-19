package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for contact display data.
 * GET /api/contacts
 */
public record ContactDisplayResponse(
    UUID ID,
    UUID contactUserId,
    String displayName,
    String contactUsername,
    String contactPhoneNumber) {
  /**
   * Creates a ContactDisplayResponse from a JSON string.
   */
  public static ContactDisplayResponse fromJson(String json) {
    Map<String, Object> map = JsonMapper.parseJson(json);
    return fromMap(map);
  }

  /**
   * Creates a ContactDisplayResponse from a parsed JSON map.
   */
  public static ContactDisplayResponse fromMap(Map<String, Object> map) {
    UUID id = JsonMapper.getUUID(map, "ID");
    // Fallback to lowercase if uppercase not found
    if (id == null) {
      id = JsonMapper.getUUID(map, "id");
    }
    UUID contactUserId = JsonMapper.getUUID(map, "contactUserId");
    String displayName = JsonMapper.getString(map, "displayName");
    String contactUsername = JsonMapper.getString(map, "contactUsername");
    String contactPhoneNumber = JsonMapper.getString(map, "contactPhoneNumber");

    return new ContactDisplayResponse(id, contactUserId, displayName, contactUsername, contactPhoneNumber);
  }

  /**
   * Parses a list of ContactDisplayResponse from JSON array string.
   */
  public static List<ContactDisplayResponse> listFromJson(String json) {
    List<ContactDisplayResponse> result = new ArrayList<>();
    if (json == null || json.trim().isEmpty()) {
      return result;
    }

    json = json.trim();
    if (!json.startsWith("[")) {
      return result;
    }

    // Simple array parsing
    Map<String, Object> wrapper = JsonMapper.parseJson("{\"items\":" + json + "}");
    List<Map<String, Object>> items = JsonMapper.getList(wrapper, "items");
    for (Map<String, Object> item : items) {
      result.add(fromMap(item));
    }

    return result;
  }

  /**
   * Gets the username (convenience accessor).
   */
  public String username() {
    return contactUsername;
  }

  /**
   * Gets the phone number (convenience accessor).
   */
  public String phoneNumber() {
    return contactPhoneNumber;
  }

  /**
   * Gets initials from the display name for avatar.
   */
  public String getInitials() {
    if (displayName == null || displayName.isEmpty())
      return "?";
    String[] parts = displayName.split(" ");
    if (parts.length >= 2) {
      return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }
    return ("" + displayName.charAt(0)).toUpperCase();
  }
}
