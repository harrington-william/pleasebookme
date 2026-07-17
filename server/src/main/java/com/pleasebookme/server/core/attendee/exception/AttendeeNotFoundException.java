package com.pleasebookme.server.core.attendee.exception;

public class AttendeeNotFoundException extends RuntimeException {
    public AttendeeNotFoundException(String message) {
        super(message);
    }
}
