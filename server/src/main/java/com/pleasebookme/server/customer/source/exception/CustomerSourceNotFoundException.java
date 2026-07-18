package com.pleasebookme.server.customer.source.exception;

public class CustomerSourceNotFoundException extends RuntimeException {
    public CustomerSourceNotFoundException(String message) {
        super(message);
    }
}
