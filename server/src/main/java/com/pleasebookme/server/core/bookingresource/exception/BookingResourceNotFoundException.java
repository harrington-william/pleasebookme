package com.pleasebookme.server.core.bookingresource.exception;

public class BookingResourceNotFoundException extends RuntimeException {
    public BookingResourceNotFoundException(String message) {
        super(message);
    }
}
