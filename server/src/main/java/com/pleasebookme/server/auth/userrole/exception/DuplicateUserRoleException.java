package com.pleasebookme.server.auth.userrole.exception;

public class DuplicateUserRoleException extends RuntimeException {
    public DuplicateUserRoleException(String message) {
        super(message);
    }
}
