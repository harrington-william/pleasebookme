package com.pleasebookme.server.core.bookingresource.exception;

public class DuplicateBookingResourceException extends RuntimeException {
    public DuplicateBookingResourceException(String message) {
        super(message);
    }
}
