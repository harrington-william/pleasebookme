package com.pleasebookme.server.security.identity.context.exception;

public class UnauthenticatedException extends RuntimeException {
    public UnauthenticatedException(String message) {
        super(message);
    }
}
