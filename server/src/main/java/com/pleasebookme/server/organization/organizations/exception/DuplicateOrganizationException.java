package com.pleasebookme.server.organization.organizations.exception;

public class DuplicateOrganizationException extends RuntimeException {
    public DuplicateOrganizationException(String message) {
        super(message);
    }
}
