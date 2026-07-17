package com.pleasebookme.server.auth.password.exception;

public class DuplicateUserPasswordException extends RuntimeException {
    public DuplicateUserPasswordException(String message) {
        super(message);
    }
}
