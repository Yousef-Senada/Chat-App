package com.untitled.api;

/**
 * Custom exception for API errors.
 * Contains HTTP status code and error message from the server.
 */
public class ApiException extends RuntimeException {

    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isUnauthorized() {
        return statusCode == 401;
    }

    public boolean isForbidden() {
        return statusCode == 403;
    }

    public boolean isNotFound() {
        return statusCode == 404;
    }

    public boolean isBadRequest() {
        return statusCode == 400;
    }

    public boolean isServerError() {
        return statusCode >= 500;
    }
}
