package com.pleasebookme.server.audit.actor.exception;

public class AuditActorNotFoundException extends RuntimeException {
    public AuditActorNotFoundException(String message) {
        super(message);
    }
}
