package com.untitled.api.endpoints;

import com.untitled.api.ApiClient;
import com.untitled.dto.request.SendMessageRequest;
import com.untitled.dto.request.UpdateMessageRequest;
import com.untitled.dto.response.MessageDisplayDto;
import com.untitled.dto.response.PageResponse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MessagesApi {

  private final ApiClient client;

  public MessagesApi(ApiClient client) {
    this.client = client;
  }

  public CompletableFuture<PageResponse<MessageDisplayDto>> getChatMessages(UUID chatId, int page, int size) {
    String path = "/api/messages/" + chatId + "?page=" + page + "&size=" + size;
    return client.get(path)
        .thenApply(json -> PageResponse.fromJson(json, MessageDisplayDto::fromMap));
  }

  public CompletableFuture<String> sendMessage(SendMessageRequest request) {
    return client.post("/api/messages", request);
  }

  public CompletableFuture<String> sendTextMessage(UUID chatId, String content) {
    SendMessageRequest request = new SendMessageRequest(chatId, "TEXT", content, null);
    return sendMessage(request);
  }

  public CompletableFuture<String> sendImageMessage(UUID chatId, String caption, String mediaUrl) {
    SendMessageRequest request = new SendMessageRequest(chatId, "IMAGE", caption, mediaUrl);
    return sendMessage(request);
  }

  public CompletableFuture<String> editMessage(UUID messageId, String newContent) {
    UpdateMessageRequest request = new UpdateMessageRequest(messageId, newContent);
    return client.patch("/api/messages", request);
  }

  public CompletableFuture<String> deleteMessage(UUID messageId) {
    return client.delete("/api/messages/" + messageId);
  }
}
