package com.pleasebookme.server.notification.queue.exception;

public class NotificationQueueNotFoundException extends RuntimeException {
    public NotificationQueueNotFoundException(String message) {
        super(message);
    }
}
