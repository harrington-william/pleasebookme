package com.pleasebookme.server.tenant.tenants.exception;

public class DuplicateTenantException extends RuntimeException {
    public DuplicateTenantException(String message) {
        super(message);
    }
}
