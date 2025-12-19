package com.example.chat_app.model.dto;

import java.util.List;
import java.util.UUID;

public record CreateChatRequest(
    String chatType,
    String groupName,
    String groupImage,
    List<UUID> membersId
){}