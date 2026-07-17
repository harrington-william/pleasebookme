package com.pleasebookme.server.integration.sheets.exception;

public class DestinationSheetsNotFoundException extends RuntimeException {
    public DestinationSheetsNotFoundException(String message) {
        super(message);
    }
}
