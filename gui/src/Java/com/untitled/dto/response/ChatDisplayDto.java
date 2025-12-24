package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ChatDisplayDto(
    UUID chatId,
    String chatType,
    String groupName,
    String groupImage,
    List<MemberDisplayDto> members) {
  public static ChatDisplayDto fromJson(String json) {
    Map<String, Object> map = JsonMapper.parseJson(json);
    return fromMap(map);
  }

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

  public static List<ChatDisplayDto> listFromJson(String json) {
    List<ChatDisplayDto> result = new ArrayList<>();
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

  public String getDisplayName() {
    if ("GROUP".equals(chatType)) {
      return groupName != null ? groupName : "Group Chat";
    }
    if (members != null && !members.isEmpty()) {
      return members.get(0).name();
    }
    return "Chat";
  }

  public boolean isGroup() {
    return "GROUP".equals(chatType);
  }
}
