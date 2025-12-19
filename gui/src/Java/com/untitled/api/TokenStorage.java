package com.untitled.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

/**
 * Manages JWT token persistence using Java Preferences API with file backup.
 */
public class TokenStorage {

  private static final String TOKEN_KEY = "jwt_token";
  private static final String TOKEN_FILE = "chat_token.dat";
  private static final String APP_DIR = ".chatapp";

  private String token;
  private final Preferences prefs;
  private final Path tokenFilePath;

  public TokenStorage() {
    this.prefs = Preferences.userNodeForPackage(TokenStorage.class);
    this.tokenFilePath = Paths.get(System.getProperty("user.home"), APP_DIR, TOKEN_FILE);
    loadToken();
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
    saveToken();
  }

  public void clearToken() {
    this.token = null;
    prefs.remove(TOKEN_KEY);
    try {
      Files.deleteIfExists(tokenFilePath);
    } catch (IOException ignored) {
      // Ignore deletion errors
    }
  }

  public boolean hasToken() {
    return token != null && !token.isEmpty();
  }

  private void loadToken() {
    // Try preferences first
    this.token = prefs.get(TOKEN_KEY, null);

    // Fallback to file if preferences is empty
    if (token == null && Files.exists(tokenFilePath)) {
      try {
        this.token = Files.readString(tokenFilePath).trim();
      } catch (IOException ignored) {
        // Ignore read errors
      }
    }
  }

  private void saveToken() {
    if (token != null) {
      prefs.put(TOKEN_KEY, token);
      try {
        Files.createDirectories(tokenFilePath.getParent());
        Files.writeString(tokenFilePath, token);
      } catch (IOException ignored) {
        // Ignore write errors, preferences is the primary storage
      }
    }
  }
}
