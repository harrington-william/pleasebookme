package com.pleasebookme.server.security.identity.context.organization.exception;

public class AmbiguousOrganizationContextException extends RuntimeException {
    public AmbiguousOrganizationContextException(String message) {
        super(message);
    }
}
