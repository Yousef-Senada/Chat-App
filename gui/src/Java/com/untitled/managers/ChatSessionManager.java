package com.untitled.managers;

import com.untitled.models.Chat;
import com.untitled.models.Message;
import com.untitled.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatSessionManager {
    
    private static ChatSessionManager instance;
    private Chat activeChat;
    private List<Chat> recentChats;
    private List<User> contacts;

    private ChatSessionManager() {
        initializeMockData();
    }

    public static ChatSessionManager getInstance() {
        if (instance == null) {
            instance = new ChatSessionManager();
        }
        return instance;
    }

    private void initializeMockData() {
        contacts = new ArrayList<>();
        contacts.add(new User("c1", "Liza Horner", "liza@email.com", "#3B82F6"));
        contacts.add(new User("c2", "Andrew Symonds", "andrew@email.com", "#22C55E"));
        contacts.add(new User("c3", "Rebecca Hallman", "rebecca@email.com", "#EF4444"));
        contacts.add(new User("c4", "Mark Simon", "mark@email.com", "#F59E0B"));
        contacts.add(new User("c5", "Sarah Johnson", "sarah@email.com", "#EC4899"));
        contacts.add(new User("c6", "David Chen", "david@email.com", "#06B6D4"));
        contacts.add(new User("c7", "Emma Wilson", "emma@email.com", "#8B5CF6"));
        contacts.add(new User("c8", "James Brown", "james@email.com", "#10B981"));

        contacts.get(0).setOnline(true);
        contacts.get(1).setOnline(true);
        contacts.get(2).setOnline(false);
        contacts.get(3).setOnline(true);
        contacts.get(4).setOnline(false);
        contacts.get(5).setOnline(true);
        contacts.get(6).setOnline(false);
        contacts.get(7).setOnline(true);

        recentChats = new ArrayList<>();
        
        Chat chat1 = new Chat("chat1", "Liza Horner", "#3B82F6");
        chat1.setLastMessage("Are you free tomorrow?");
        chat1.setLastMessageTime(LocalDateTime.now().minusMinutes(5));
        chat1.setUnreadCount(2);
        addMessagesToChat(chat1);
        recentChats.add(chat1);

        Chat chat2 = new Chat("chat2", "Andrew Symonds", "#22C55E");
        chat2.setLastMessage("The project looks great!");
        chat2.setLastMessageTime(LocalDateTime.now().minusHours(1));
        recentChats.add(chat2);

        Chat chat3 = new Chat("chat3", "Design Team", "#8B5CF6", true);
        chat3.setLastMessage("Meeting at 3pm");
        chat3.setLastMessageTime(LocalDateTime.now().minusHours(2));
        chat3.setUnreadCount(5);
        recentChats.add(chat3);

        Chat chat4 = new Chat("chat4", "Rebecca Hallman", "#EF4444");
        chat4.setLastMessage("Thanks for your help!");
        chat4.setLastMessageTime(LocalDateTime.now().minusDays(1));
        recentChats.add(chat4);

        Chat chat5 = new Chat("chat5", "Mark Simon", "#F59E0B");
        chat5.setLastMessage("See you later");
        chat5.setLastMessageTime(LocalDateTime.now().minusDays(1));
        recentChats.add(chat5);

        Chat chat6 = new Chat("chat6", "Marketing Team", "#EC4899", true);
        chat6.setLastMessage("New campaign launched!");
        chat6.setLastMessageTime(LocalDateTime.now().minusDays(2));
        recentChats.add(chat6);
    }

    private void addMessagesToChat(Chat chat) {
        Message msg1 = Message.createTextMessage("m1", "c1", "Liza Horner", "Hello there, How are you?", false);
        msg1.setTimestamp(LocalDateTime.now().minusMinutes(30));
        chat.addMessage(msg1);

        Message msg2 = Message.createTextMessage("m2", "c1", "Liza Horner", "Are you Jonathan Doe?", false);
        msg2.setTimestamp(LocalDateTime.now().minusMinutes(25));
        chat.addMessage(msg2);

        Message msg3 = Message.createTextMessage("m3", "user-1", "Me", "I'm Fine, Yes indeed", true);
        msg3.setTimestamp(LocalDateTime.now().minusMinutes(20));
        chat.addMessage(msg3);

        Message msg4 = Message.createTextMessage("m4", "c1", "Liza Horner", "Are you free tomorrow?", false);
        msg4.setTimestamp(LocalDateTime.now().minusMinutes(15));
        chat.addMessage(msg4);

        Message msg5 = Message.createTextMessage("m5", "user-1", "Me", "Sorry no", true);
        msg5.setTimestamp(LocalDateTime.now().minusMinutes(10));
        chat.addMessage(msg5);
    }

    public List<Chat> getRecentChats() {
        return new ArrayList<>(recentChats);
    }

    public List<User> getContacts() {
        return new ArrayList<>(contacts);
    }

    public void setActiveChat(Chat chat) {
        this.activeChat = chat;
    }

    public Chat getActiveChat() {
        return activeChat;
    }

    public List<Message> getMessages(String chatId) {
        for (Chat chat : recentChats) {
            if (chat.getId().equals(chatId)) {
                return chat.getMessages();
            }
        }
        return new ArrayList<>();
    }

    public void sendMessage(String content) {
        if (activeChat != null && content != null && !content.trim().isEmpty()) {
            String msgId = "m" + System.currentTimeMillis();
            Message msg = Message.createTextMessage(msgId, "user-1", "Me", content, true);
            activeChat.addMessage(msg);
        }
    }

    public void sendImageMessage(String filePath, String fileName, String fileSize) {
        if (activeChat != null) {
            String msgId = "m" + System.currentTimeMillis();
            Message msg = Message.createImageMessage(msgId, "user-1", "Me", filePath, fileName, fileSize, true);
            activeChat.addMessage(msg);
        }
    }

    public void sendVideoMessage(String filePath, String videoName, String fileSize) {
        if (activeChat != null) {
            String msgId = "m" + System.currentTimeMillis();
            Message msg = Message.createVideoMessage(msgId, "user-1", "Me", filePath, videoName, fileSize, true);
            activeChat.addMessage(msg);
        }
    }

    public void sendFileMessage(String filePath, String fileName, String fileSize) {
        if (activeChat != null) {
            String msgId = "m" + System.currentTimeMillis();
            Message msg = Message.createFileMessage(msgId, "user-1", "Me", filePath, fileName, fileSize, true);
            activeChat.addMessage(msg);
        }
    }

    public Chat getChatById(String chatId) {
        for (Chat chat : recentChats) {
            if (chat.getId().equals(chatId)) {
                return chat;
            }
        }
        return null;
    }

    public Chat createChatWithContact(User contact) {
        for (Chat chat : recentChats) {
            if (!chat.isGroup() && chat.getName().equals(contact.getName())) {
                return chat;
            }
        }
        
        String chatId = "chat" + System.currentTimeMillis();
        Chat newChat = new Chat(chatId, contact.getName(), contact.getAvatarColor());
        newChat.addParticipant(contact);
        recentChats.add(0, newChat);
        return newChat;
    }

    public Chat createGroupChat(String name, String avatarColor) {
        String chatId = "chat" + System.currentTimeMillis();
        Chat newChat = new Chat(chatId, name, avatarColor, true);
        recentChats.add(0, newChat);
        return newChat;
    }
}
