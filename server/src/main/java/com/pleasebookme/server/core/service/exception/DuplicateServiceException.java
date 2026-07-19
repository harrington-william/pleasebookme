package com.pleasebookme.server.core.service.exception;

public class DuplicateServiceException extends RuntimeException {
    public DuplicateServiceException(String message) {
        super(message);
    }
}
