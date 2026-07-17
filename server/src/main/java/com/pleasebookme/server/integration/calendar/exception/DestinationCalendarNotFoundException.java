package com.pleasebookme.server.integration.calendar.exception;

public class DestinationCalendarNotFoundException extends RuntimeException {
    public DestinationCalendarNotFoundException(String message) {
        super(message);
    }
}
