package com.pleasebookme.server.customer.activity.exception;

public class CustomerActivityNotFoundException extends RuntimeException {
    public CustomerActivityNotFoundException(String message) {
        super(message);
    }
}
