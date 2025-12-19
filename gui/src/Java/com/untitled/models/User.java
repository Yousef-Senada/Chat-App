package com.untitled.models;

public class User {
    private String id;
    private String name;
    private String email;
    private String status;
    private String avatarColor;
    private boolean online;

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = "Available";
        this.avatarColor = "#8B5CF6";
        this.online = true;
    }

    public User(String id, String name, String email, String avatarColor) {
        this(id, name, email);
        this.avatarColor = avatarColor;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAvatarColor() { return avatarColor; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public String getInitials() {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return ("" + name.charAt(0)).toUpperCase();
    }
}
