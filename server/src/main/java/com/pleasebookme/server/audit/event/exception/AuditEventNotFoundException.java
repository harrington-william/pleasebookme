package com.pleasebookme.server.audit.event.exception;

public class AuditEventNotFoundException extends RuntimeException {
    public AuditEventNotFoundException(String message) {
        super(message);
    }
}
