package com.untitled.models;

import java.util.ArrayList;
import java.util.List;

public class Group {
    
    public enum GroupType {
        PUBLIC,
        PRIVATE,
        ADMIN
    }

    private String id;
    private String name;
    private String description;
    private String category;
    private String avatarColor;
    private GroupType type;
    private User creator;
    private List<User> members;
    private List<User> admins;

    public Group(String id, String name, String description, GroupType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.avatarColor = "#8B5CF6";
        this.members = new ArrayList<>();
        this.admins = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAvatarColor() { return avatarColor; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }

    public GroupType getType() { return type; }
    public void setType(GroupType type) { this.type = type; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public List<User> getMembers() { return members; }
    public void addMember(User user) { this.members.add(user); }
    public void removeMember(User user) { this.members.remove(user); }

    public List<User> getAdmins() { return admins; }
    public void addAdmin(User user) { 
        this.admins.add(user);
        if (!members.contains(user)) {
            members.add(user);
        }
    }

    public boolean isAdmin(User user) {
        return admins.stream().anyMatch(a -> a.getId().equals(user.getId()));
    }

    public boolean canJoin(User user) {
        switch (type) {
            case PUBLIC: return true;
            case PRIVATE: return false;
            case ADMIN: return false;
            default: return false;
        }
    }

    public int getMemberCount() {
        return members.size();
    }

    public String getTypeDisplayName() {
        switch (type) {
            case PUBLIC: return "Public Group";
            case PRIVATE: return "Private Group";
            case ADMIN: return "Admin Group";
            default: return "Group";
        }
    }
}
