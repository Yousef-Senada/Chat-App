package com.untitled.dto.response;

import com.untitled.util.JsonMapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for message display data.
 * GET /api/messages/chat/{chatId}
 */
public record MessageDisplayDto(
    UUID messageId,
    SenderDto sender,
    String messageType,
    String content,
    String mediaUrl,
    LocalDateTime timestamp,
    boolean isEdited,
    boolean isDeleted) {
  /**
   * Creates a MessageDisplayDto from a JSON string.
   */
  public static MessageDisplayDto fromJson(String json) {
    Map<String, Object> map = JsonMapper.parseJson(json);
    return fromMap(map);
  }

  /**
   * Creates a MessageDisplayDto from a parsed JSON map.
   */
  public static MessageDisplayDto fromMap(Map<String, Object> map) {
    UUID messageId = JsonMapper.getUUID(map, "messageId");

    Map<String, Object> senderMap = JsonMapper.getMap(map, "sender");
    SenderDto sender = SenderDto.fromMap(senderMap);

    String messageType = JsonMapper.getString(map, "messageType");
    String content = JsonMapper.getString(map, "content");
    String mediaUrl = JsonMapper.getString(map, "mediaUrl");
    LocalDateTime timestamp = JsonMapper.getDateTime(map, "timestamp");
    boolean isEdited = JsonMapper.getBoolean(map, "isEdited");
    boolean isDeleted = JsonMapper.getBoolean(map, "isDeleted");

    return new MessageDisplayDto(messageId, sender, messageType, content, mediaUrl, timestamp, isEdited, isDeleted);
  }

  /**
   * Checks if this is a text message.
   */
  public boolean isText() {
    return "TEXT".equals(messageType);
  }

  /**
   * Checks if this is an image message.
   */
  public boolean isImage() {
    return "IMAGE".equals(messageType);
  }

  /**
   * Checks if this is an audio message.
   */
  public boolean isAudio() {
    return "AUDIO".equals(messageType);
  }

  /**
   * Gets the display content for this message.
   */
  public String getDisplayContent() {
    if (isDeleted) {
      return "[Message deleted]";
    }
    if (content != null) {
      return content;
    }
    return switch (messageType) {
      case "IMAGE" -> "[Image]";
      case "AUDIO" -> "[Audio]";
      default -> "";
    };
  }
}
