package com.pleasebookme.server.auth.password.exception;

public class UserPasswordNotFoundException extends RuntimeException {
    public UserPasswordNotFoundException(String message) {
        super(message);
    }
}
