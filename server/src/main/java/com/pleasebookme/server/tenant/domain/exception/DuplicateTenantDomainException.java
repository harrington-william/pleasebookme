package com.pleasebookme.server.tenant.domain.exception;

public class DuplicateTenantDomainException extends RuntimeException {
    public DuplicateTenantDomainException(String message) {
        super(message);
    }
}
