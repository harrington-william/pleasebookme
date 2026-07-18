package com.pleasebookme.server.notification.channel.exception;

public class NotificationChannelNotFoundException extends RuntimeException {
    public NotificationChannelNotFoundException(String message) {
        super(message);
    }
}
