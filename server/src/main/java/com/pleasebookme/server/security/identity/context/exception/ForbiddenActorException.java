package com.pleasebookme.server.security.identity.context.exception;

public class ForbiddenActorException extends RuntimeException {
    public ForbiddenActorException(String message) {
        super(message);
    }
}
