package com.pleasebookme.server.security.identity.exception;

public class ForbiddenActorException extends RuntimeException {
    public ForbiddenActorException(String message) {
        super(message);
    }
}
