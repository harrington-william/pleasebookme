package com.pleasebookme.server.service.integration.exception;

public class OAuthConnectionAccessDeniedException extends RuntimeException {
    public OAuthConnectionAccessDeniedException(String message) {
        super(message);
    }
}
