package com.untitled.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    
    public enum MessageType {
        TEXT, IMAGE, VIDEO, FILE
    }

    private String id;
    private String senderId;
    private String senderName;
    private String content;
    private MessageType type;
    private LocalDateTime timestamp;
    private boolean outgoing;
    private String fileName;
    private String fileSize;
    private String filePath;

    public Message(String id, String senderId, String senderName, String content, boolean outgoing) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.type = MessageType.TEXT;
        this.timestamp = LocalDateTime.now();
        this.outgoing = outgoing;
    }

    public static Message createTextMessage(String id, String senderId, String senderName, String content, boolean outgoing) {
        return new Message(id, senderId, senderName, content, outgoing);
    }

    public static Message createImageMessage(String id, String senderId, String senderName, String filePath, String fileName, String fileSize, boolean outgoing) {
        Message msg = new Message(id, senderId, senderName, "[Image]", outgoing);
        msg.type = MessageType.IMAGE;
        msg.filePath = filePath;
        msg.fileName = fileName;
        msg.fileSize = fileSize;
        return msg;
    }

    public static Message createVideoMessage(String id, String senderId, String senderName, String filePath, String videoName, String fileSize, boolean outgoing) {
        Message msg = new Message(id, senderId, senderName, "[Video]", outgoing);
        msg.type = MessageType.VIDEO;
        msg.filePath = filePath;
        msg.fileName = videoName;
        msg.fileSize = fileSize;
        return msg;
    }

    public static Message createFileMessage(String id, String senderId, String senderName, String filePath, String fileName, String fileSize, boolean outgoing) {
        Message msg = new Message(id, senderId, senderName, fileName, outgoing);
        msg.type = MessageType.FILE;
        msg.filePath = filePath;
        msg.fileName = fileName;
        msg.fileSize = fileSize;
        return msg;
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getContent() { return content; }
    public MessageType getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public boolean isOutgoing() { return outgoing; }
    public String getFileName() { return fileName; }
    public String getFileSize() { return fileSize; }
    public String getFilePath() { return filePath; }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getFormattedDateTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM, yyyy, HH:mm"));
    }
}
