package com.pleasebookme.server.audit.actor.exception;

public class DuplicateAuditActorException extends RuntimeException {
    public DuplicateAuditActorException(String message) {
        super(message);
    }
}
