package com.pleasebookme.server.audit.change.exception;

public class AuditChangeNotFoundException extends RuntimeException {
    public AuditChangeNotFoundException(String message) {
        super(message);
    }
}
