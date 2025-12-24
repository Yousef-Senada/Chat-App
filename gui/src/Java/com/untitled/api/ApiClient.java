package com.untitled.api;

import com.untitled.util.JsonMapper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

public class ApiClient {

  private static final Logger LOGGER = Logger.getLogger(ApiClient.class.getName());
  private static final String BASE_URL = "http://localhost:8080";
  private final HttpClient httpClient;
  private final TokenStorage tokenStorage;

  public ApiClient(TokenStorage tokenStorage) {
    this.tokenStorage = tokenStorage;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
  }

  public CompletableFuture<String> get(String path) {
    HttpRequest request = buildRequest(path)
        .GET()
        .build();
    return sendAsync(request);
  }

  public CompletableFuture<String> post(String path, Object body) {
    String jsonBody = JsonMapper.toJson(body);
    LOGGER.fine("POST body: " + jsonBody);
    HttpRequest request = buildRequest(path)
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
        .build();
    return sendAsync(request);
  }

  public CompletableFuture<String> patch(String path, Object body) {
    String jsonBody = JsonMapper.toJson(body);
    LOGGER.fine("PATCH body: " + jsonBody);
    HttpRequest request = buildRequest(path)
        .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
        .build();
    return sendAsync(request);
  }

  public CompletableFuture<String> delete(String path) {
    HttpRequest request = buildRequest(path)
        .DELETE()
        .build();
    return sendAsync(request);
  }

  public CompletableFuture<String> deleteWithBody(String path, Object body) {
    String jsonBody = JsonMapper.toJson(body);
    LOGGER.fine("DELETE body: " + jsonBody);
    HttpRequest request = buildRequest(path)
        .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonBody))
        .build();
    return sendAsync(request);
  }

  private HttpRequest.Builder buildRequest(String path) {
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .uri(URI.create(BASE_URL + path))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .timeout(Duration.ofSeconds(30));

    String token = tokenStorage.getToken();
    if (token != null && !token.isEmpty()) {
      builder.header("Authorization", "Bearer " + token);
    }

    return builder;
  }

  private CompletableFuture<String> sendAsync(HttpRequest request) {
    LOGGER.info("API Request: " + request.method() + " " + request.uri());

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(response -> {
          int statusCode = response.statusCode();
          String body = response.body();

          LOGGER.info("API Response: " + statusCode + " for " + request.uri());
          LOGGER.fine("Response body: " + (body != null && body.length() < 500 ? body : "[truncated]"));

          if (statusCode >= 400) {
            String errorMessage = extractErrorMessage(body, statusCode);
            LOGGER.warning("API Error: " + statusCode + " - " + errorMessage);
            throw new ApiException(statusCode, errorMessage);
          }

          return body;
        })
        .exceptionally(ex -> {
          Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;

          if (cause instanceof ApiException) {
            throw (ApiException) cause;
          }

          if (cause instanceof ConnectException) {
            LOGGER.severe("Connection refused - is the server running? " + request.uri());
            throw new ApiException(0, "Cannot connect to server. Please check if the server is running.");
          }
          if (cause instanceof HttpTimeoutException) {
            LOGGER.severe("Request timeout for " + request.uri());
            throw new ApiException(0, "Request timed out. Please try again.");
          }
          if (cause instanceof IOException) {
            LOGGER.severe("Network error for " + request.uri() + ": " + cause.getMessage());
            throw new ApiException(0, "Network error. Please check your connection.");
          }

          LOGGER.severe("Unexpected error for " + request.uri() + ": " + cause);
          throw new ApiException(0, "An unexpected error occurred: " + cause.getMessage());
        });
  }

  private String extractErrorMessage(String body, int statusCode) {
    if (body == null || body.isEmpty()) {
      return getDefaultErrorMessage(statusCode);
    }

    try {
      Map<String, Object> errorMap = JsonMapper.parseJson(body);
      String message = JsonMapper.getString(errorMap, "message");
      if (message != null && !message.isEmpty()) {
        return message;
      }
      String error = JsonMapper.getString(errorMap, "error");
      if (error != null && !error.isEmpty()) {
        return error;
      }
    } catch (Exception e) {
      LOGGER.fine("Could not parse error body as JSON: " + e.getMessage());
    }

    if (body.length() < 200) {
      return body;
    }

    return getDefaultErrorMessage(statusCode);
  }

  private String getDefaultErrorMessage(int statusCode) {
    return switch (statusCode) {
      case 400 -> "Bad request";
      case 401 -> "Unauthorized - please log in again";
      case 403 -> "Access denied";
      case 404 -> "Resource not found";
      case 500 -> "Server error - please try again later";
      default -> "Request failed with status " + statusCode;
    };
  }
}
