package com.untitled.managers;

import com.untitled.models.User;

public class AuthSessionManager {
    
    private static AuthSessionManager instance;
    private User currentUser;
    private boolean loggedIn;

    private AuthSessionManager() {
        this.loggedIn = false;
    }

    public static AuthSessionManager getInstance() {
        if (instance == null) {
            instance = new AuthSessionManager();
        }
        return instance;
    }

    public boolean login(String email, String password) {
        String name = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        
        this.currentUser = new User("user-1", name, email, "#8B5CF6");
        this.currentUser.setStatus("Online");
        this.currentUser.setOnline(true);
        this.loggedIn = true;
        
        System.out.println("User logged in: " + name);
        return true;
    }

    public boolean register(String name, String email, String password) {
        this.currentUser = new User("user-1", name, email, "#8B5CF6");
        this.currentUser.setStatus("Online");
        this.currentUser.setOnline(true);
        this.loggedIn = true;
        
        System.out.println("User registered: " + name);
        return true;
    }

    public void logout() {
        this.currentUser = null;
        this.loggedIn = false;
        System.out.println("User logged out");
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void updateProfile(String name, String status) {
        if (currentUser != null) {
            currentUser.setName(name);
            currentUser.setStatus(status);
        }
    }
}
