package com.pleasebookme.server.auth.user.exception;

public class UserPhoneNumberAlreadyExistException extends RuntimeException {
    public UserPhoneNumberAlreadyExistException(String message) {
        super(message);
    }
}
