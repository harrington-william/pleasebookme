package com.pleasebookme.server.service.organization.exception;

public class NoOrganizationMembershipException extends RuntimeException {
    public NoOrganizationMembershipException(String message) {
        super(message);
    }
}
