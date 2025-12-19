package com.untitled.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String id;
    private String name;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private String avatarColor;
    private boolean isGroup;
    private int unreadCount;
    private List<User> participants;
    private List<Message> messages;

    public Chat(String id, String name, String avatarColor) {
        this.id = id;
        this.name = name;
        this.avatarColor = avatarColor;
        this.isGroup = false;
        this.unreadCount = 0;
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.lastMessageTime = LocalDateTime.now();
    }

    public Chat(String id, String name, String avatarColor, boolean isGroup) {
        this(id, name, avatarColor);
        this.isGroup = isGroup;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public String getAvatarColor() { return avatarColor; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }

    public boolean isGroup() { return isGroup; }
    public void setGroup(boolean group) { isGroup = group; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public List<User> getParticipants() { return participants; }
    public void addParticipant(User user) { this.participants.add(user); }

    public List<Message> getMessages() { return messages; }
    public void addMessage(Message message) { 
        this.messages.add(message);
        this.lastMessage = message.getContent();
        this.lastMessageTime = message.getTimestamp();
    }

    public String getInitials() {
        if (name == null || name.isEmpty()) return "?";
        if (isGroup) return name.substring(0, Math.min(2, name.length())).toUpperCase();
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return ("" + name.charAt(0)).toUpperCase();
    }

    public String getFormattedTime() {
        if (lastMessageTime == null) return "";
        LocalDateTime now = LocalDateTime.now();
        if (lastMessageTime.toLocalDate().equals(now.toLocalDate())) {
            return lastMessageTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        }
        return lastMessageTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
    }
}
