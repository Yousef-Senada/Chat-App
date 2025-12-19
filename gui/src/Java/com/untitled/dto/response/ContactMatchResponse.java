package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for contact matching (find by phone, sync contacts).
 * GET /api/contacts/phone
 * POST /api/contacts/sync
 */
public record ContactMatchResponse(
    UUID id,
    String userName,
    String name,
    String phoneNumber) {
  /**
   * Creates a ContactMatchResponse from a JSON string.
   */
  public static ContactMatchResponse fromJson(String json) {
    Map<String, Object> map = JsonMapper.parseJson(json);
    return fromMap(map);
  }

  /**
   * Creates a ContactMatchResponse from a parsed JSON map.
   */
  public static ContactMatchResponse fromMap(Map<String, Object> map) {
    UUID id = JsonMapper.getUUID(map, "id");
    String userName = JsonMapper.getString(map, "userName");
    String name = JsonMapper.getString(map, "name");
    String phoneNumber = JsonMapper.getString(map, "phoneNumber");
    return new ContactMatchResponse(id, userName, name, phoneNumber);
  }

  /**
   * Parses a list of ContactMatchResponse from JSON array string.
   */
  public static List<ContactMatchResponse> listFromJson(String json) {
    List<ContactMatchResponse> result = new ArrayList<>();
    if (json == null || json.trim().isEmpty()) {
      return result;
    }

    json = json.trim();
    if (!json.startsWith("[")) {
      return result;
    }

    Map<String, Object> wrapper = JsonMapper.parseJson("{\"items\":" + json + "}");
    List<Map<String, Object>> items = JsonMapper.getList(wrapper, "items");
    for (Map<String, Object> item : items) {
      result.add(fromMap(item));
    }

    return result;
  }

  /**
   * Gets initials from the name for avatar display.
   */
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
