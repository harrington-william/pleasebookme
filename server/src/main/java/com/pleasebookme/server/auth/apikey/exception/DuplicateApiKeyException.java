package com.pleasebookme.server.auth.apikey.exception;

public class DuplicateApiKeyException extends RuntimeException {
    public DuplicateApiKeyException(String message) {
        super(message);
    }
}
