package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for chat display data.
 * GET /api/chats
 */
public record ChatDisplayDto(
    UUID chatId,
    String chatType,
    String groupName,
    String groupImage,
    List<MemberDisplayDto> members) {
  /**
   * Creates a ChatDisplayDto from a JSON string.
   */
  public static ChatDisplayDto fromJson(String json) {
    Map<String, Object> map = JsonMapper.parseJson(json);
    return fromMap(map);
  }

  /**
   * Creates a ChatDisplayDto from a parsed JSON map.
   */
  public static ChatDisplayDto fromMap(Map<String, Object> map) {
    UUID chatId = JsonMapper.getUUID(map, "chatId");
    String chatType = JsonMapper.getString(map, "chatType");
    String groupName = JsonMapper.getString(map, "groupName");
    String groupImage = JsonMapper.getString(map, "groupImage");

    List<MemberDisplayDto> members = new ArrayList<>();
    List<Map<String, Object>> membersList = JsonMapper.getList(map, "members");
    for (Map<String, Object> memberMap : membersList) {
      members.add(MemberDisplayDto.fromMap(memberMap));
    }

    return new ChatDisplayDto(chatId, chatType, groupName, groupImage, members);
  }

  /**
   * Parses a list of ChatDisplayDto from JSON array string.
   */
  public static List<ChatDisplayDto> listFromJson(String json) {
    List<ChatDisplayDto> result = new ArrayList<>();
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
   * Returns the display name for this chat.
   * For P2P chats, returns the other member's name.
   * For group chats, returns the group name.
   */
  public String getDisplayName() {
    if ("GROUP".equals(chatType)) {
      return groupName != null ? groupName : "Group Chat";
    }
    // For P2P, return first member's name (the other person)
    if (members != null && !members.isEmpty()) {
      return members.get(0).name();
    }
    return "Chat";
  }

  /**
   * Checks if this is a group chat.
   */
  public boolean isGroup() {
    return "GROUP".equals(chatType);
  }
}
