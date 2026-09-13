package com.pleasebookme.server.core.bookingpolicy.exception;

public class DuplicateBookingPolicyException extends RuntimeException {
    public DuplicateBookingPolicyException(String message) {
        super(message);
    }
}
