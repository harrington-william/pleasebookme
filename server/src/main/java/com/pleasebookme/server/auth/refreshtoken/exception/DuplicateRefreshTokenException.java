package com.pleasebookme.server.auth.refreshtoken.exception;

public class DuplicateRefreshTokenException extends RuntimeException {
    public DuplicateRefreshTokenException(String message) {
        super(message);
    }
}
