package com.pleasebookme.server.service.auth.exception;

public class GoogleAccountEmailNotVerifiedException extends RuntimeException {
    public GoogleAccountEmailNotVerifiedException(String message) {
        super(message);
    }
}
