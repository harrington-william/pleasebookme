package com.pleasebookme.server.security.oauth.google.exception;

public class InvalidGoogleIdTokenException extends RuntimeException {
    public InvalidGoogleIdTokenException(String message) {
        super(message);
    }
}
