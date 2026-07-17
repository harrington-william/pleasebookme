package com.pleasebookme.server.organization.profile.exception;

public class DuplicateProfileException extends RuntimeException {
    public DuplicateProfileException(String message) {
        super(message);
    }
}
