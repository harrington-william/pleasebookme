package com.pleasebookme.server.core.outofoffice.exception;

public class OutOfOfficeNotFoundException extends RuntimeException {
    public OutOfOfficeNotFoundException(String message) {
        super(message);
    }
}
