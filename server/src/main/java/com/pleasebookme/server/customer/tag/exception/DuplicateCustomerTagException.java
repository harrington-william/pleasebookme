package com.pleasebookme.server.customer.tag.exception;

public class DuplicateCustomerTagException extends RuntimeException {
    public DuplicateCustomerTagException(String message) {
        super(message);
    }
}
