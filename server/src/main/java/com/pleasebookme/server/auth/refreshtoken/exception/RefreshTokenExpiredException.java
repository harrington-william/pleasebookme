package com.pleasebookme.server.auth.refreshtoken.exception;

public class RefreshTokenExpiredException extends RuntimeException {
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
