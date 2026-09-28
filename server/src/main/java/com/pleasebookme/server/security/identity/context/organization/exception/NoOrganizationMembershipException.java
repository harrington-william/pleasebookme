package com.pleasebookme.server.security.identity.context.organization.exception;

public class NoOrganizationMembershipException extends RuntimeException {
    public NoOrganizationMembershipException(String message) {
        super(message);
    }
}
