package com.pleasebookme.server.core.bookingpolicy.exception;

public class BookingPolicyNotFoundException extends RuntimeException {
    public BookingPolicyNotFoundException(String message) {
        super(message);
    }
}
