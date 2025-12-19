package com.untitled.factories;

import com.untitled.models.Group;
import com.untitled.models.User;

import java.util.UUID;

public class GroupFactory {

    private static final String[] GROUP_COLORS = {
        "#8B5CF6", "#3B82F6", "#22C55E", "#EF4444", 
        "#F59E0B", "#EC4899", "#06B6D4", "#10B981"
    };

    private static int colorIndex = 0;

    public static Group createPublicGroup(String name, String description) {
        Group group = new Group(generateId(), name, description, Group.GroupType.PUBLIC);
        group.setAvatarColor(getNextColor());
        return group;
    }

    public static Group createPrivateGroup(String name, String description) {
        Group group = new Group(generateId(), name, description, Group.GroupType.PRIVATE);
        group.setAvatarColor(getNextColor());
        return group;
    }

    public static Group createAdminGroup(String name, String description) {
        Group group = new Group(generateId(), name, description, Group.GroupType.ADMIN);
        group.setAvatarColor(getNextColor());
        return group;
    }

    public static Group createGroup(String name, String description, Group.GroupType type) {
        switch (type) {
            case PUBLIC:
                return createPublicGroup(name, description);
            case PRIVATE:
                return createPrivateGroup(name, description);
            case ADMIN:
                return createAdminGroup(name, description);
            default:
                return createPublicGroup(name, description);
        }
    }

    public static Group createGroupWithOwner(String name, String description, 
                                              Group.GroupType type, User creator) {
        Group group = createGroup(name, description, type);
        group.setCreator(creator);
        group.addAdmin(creator);
        return group;
    }

    public static Group createGroupWithMembers(String name, String description,
                                                Group.GroupType type, User creator,
                                                User... members) {
        Group group = createGroupWithOwner(name, description, type, creator);
        for (User member : members) {
            group.addMember(member);
        }
        return group;
    }

    private static String generateId() {
        return "group-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String getNextColor() {
        String color = GROUP_COLORS[colorIndex % GROUP_COLORS.length];
        colorIndex++;
        return color;
    }

    public static void resetColorIndex() {
        colorIndex = 0;
    }
}
