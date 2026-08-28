package com.pleasebookme.server.core.service.exception;

public class AmbiguousServiceOwnerException extends RuntimeException {
    public AmbiguousServiceOwnerException(String message) {
        super(message);
    }
}
