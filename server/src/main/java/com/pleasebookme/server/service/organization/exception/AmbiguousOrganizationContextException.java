package com.pleasebookme.server.service.organization.exception;

public class AmbiguousOrganizationContextException extends RuntimeException {
    public AmbiguousOrganizationContextException(String message) {
        super(message);
    }
}
