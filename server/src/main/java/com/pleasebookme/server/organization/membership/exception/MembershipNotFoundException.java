package com.pleasebookme.server.organization.membership.exception;

public class MembershipNotFoundException extends RuntimeException {
    public MembershipNotFoundException(String message) {
        super(message);
    }
}
