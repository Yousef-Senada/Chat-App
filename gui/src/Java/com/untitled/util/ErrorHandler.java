package com.untitled.util;

import com.untitled.api.ApiException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorHandler {
  private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());

  public static String extractMessage(Throwable throwable) {
    Throwable cause = throwable.getCause();

    if (cause instanceof ApiException apiEx) {
      LOGGER.log(Level.WARNING, "API Error: " + apiEx.getStatusCode() + " - " + apiEx.getMessage());
      return apiEx.getMessage();
    }

    if (cause != null) {
      LOGGER.log(Level.WARNING, "Error: " + cause.getMessage(), cause);
      return cause.getMessage();
    }

    String message = throwable.getMessage();
    LOGGER.log(Level.WARNING, "Unexpected error: " + message, throwable);
    return message != null ? message : "An unexpected error occurred";
  }

  public static void showError(String title, String message) {
    Platform.runLater(() -> {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText(title);
      alert.setContentText(message);
      alert.showAndWait();
    });
  }

  public static void showInfo(String title, String message) {
    Platform.runLater(() -> {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Information");
      alert.setHeaderText(title);
      alert.setContentText(message);
      alert.showAndWait();
    });
  }

  public static boolean isAuthError(Throwable throwable) {
    Throwable cause = throwable.getCause();
    if (cause instanceof ApiException apiEx) {
      return apiEx.isUnauthorized();
    }
    return false;
  }

  public static boolean isConnectionError(Throwable throwable) {
    Throwable cause = throwable.getCause();
    if (cause instanceof ApiException apiEx) {
      return apiEx.getStatusCode() == 0;
    }
    return false;
  }

  public static void logRequest(String method, String url) {
    LOGGER.info("API Request: " + method + " " + url);
  }

  public static void logResponse(int statusCode, String url) {
    if (statusCode >= 400) {
      LOGGER.warning("API Error Response: " + statusCode + " for " + url);
    } else {
      LOGGER.info("API Response: " + statusCode + " for " + url);
    }
  }
}
