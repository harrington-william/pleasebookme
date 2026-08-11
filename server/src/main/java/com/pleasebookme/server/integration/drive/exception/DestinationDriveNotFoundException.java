package com.pleasebookme.server.integration.drive.exception;

public class DestinationDriveNotFoundException extends RuntimeException {
    public DestinationDriveNotFoundException(String message) {
        super(message);
    }
}
