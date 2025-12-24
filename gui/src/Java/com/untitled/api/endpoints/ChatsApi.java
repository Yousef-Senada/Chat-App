package com.untitled.api.endpoints;

import com.untitled.api.ApiClient;
import com.untitled.dto.request.CreateChatRequest;
import com.untitled.dto.request.UpdateMembershipRequest;
import com.untitled.dto.response.ChatDisplayDto;
import com.untitled.dto.response.MemberDisplayDto;
import com.untitled.util.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChatsApi {

  private final ApiClient client;

  public ChatsApi(ApiClient client) {
    this.client = client;
  }

  public CompletableFuture<List<ChatDisplayDto>> getUserChats() {
    return client.get("/api/chats")
        .thenApply(ChatDisplayDto::listFromJson);
  }

  public CompletableFuture<String> createChat(CreateChatRequest request) {
    return client.post("/api/chats", request);
  }

  public CompletableFuture<String> createP2PChat(UUID otherUserId) {
    CreateChatRequest request = new CreateChatRequest(
        "P2P", null, null, List.of(otherUserId));
    return createChat(request);
  }

  public CompletableFuture<String> createGroupChat(String groupName, String groupImage, List<UUID> memberIds) {
    CreateChatRequest request = new CreateChatRequest(
        "GROUP", groupName, groupImage, memberIds);
    return createChat(request);
  }

  public CompletableFuture<List<MemberDisplayDto>> getChatMembers(UUID chatId) {
    return client.get("/api/chats/members/" + chatId)
        .thenApply(json -> {
          List<MemberDisplayDto> result = new ArrayList<>();
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
            result.add(MemberDisplayDto.fromMap(item));
          }
          return result;
        });
  }

  public CompletableFuture<String> addMembers(UUID chatId, List<UUID> memberIds) {
    UpdateMembershipRequest request = new UpdateMembershipRequest(chatId, memberIds);
    return client.post("/api/chats/members", request);
  }

  public CompletableFuture<String> removeMembers(UUID chatId, List<UUID> memberIds) {
    UpdateMembershipRequest request = new UpdateMembershipRequest(chatId, memberIds);
    return client.deleteWithBody("/api/chats/members", request);
  }

  public CompletableFuture<String> leaveChat(UUID chatId, UUID currentUserId) {
    return removeMembers(chatId, List.of(currentUserId));
  }
}
