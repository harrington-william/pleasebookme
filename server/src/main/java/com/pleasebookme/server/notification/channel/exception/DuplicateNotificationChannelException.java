package com.pleasebookme.server.notification.channel.exception;

public class DuplicateNotificationChannelException extends RuntimeException {
    public DuplicateNotificationChannelException(String message) {
        super(message);
    }
}
