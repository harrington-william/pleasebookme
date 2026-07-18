package com.pleasebookme.server.audit.resource.exception;

public class AuditResourceNotFoundException extends RuntimeException {
    public AuditResourceNotFoundException(String message) {
        super(message);
    }
}
