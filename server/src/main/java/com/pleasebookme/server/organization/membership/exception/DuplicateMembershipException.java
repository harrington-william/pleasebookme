package com.pleasebookme.server.organization.membership.exception;

public class DuplicateMembershipException extends RuntimeException {
    public DuplicateMembershipException(String message) {
        super(message);
    }
}
