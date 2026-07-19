package com.pleasebookme.server.auth.permission.exception;

public class DuplicatePermissionException extends RuntimeException {
    public DuplicatePermissionException(String message) {
        super(message);
    }
}
