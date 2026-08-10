package com.pleasebookme.server.service.auth.exception;

public class InvalidSessionHandoffException extends RuntimeException {
    public InvalidSessionHandoffException(String message) {
        super(message);
    }
}
