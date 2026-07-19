package com.pleasebookme.server.tenant.domain.exception;

public class TenantDomainNotFoundException extends RuntimeException {
    public TenantDomainNotFoundException(String message) {
        super(message);
    }
}
