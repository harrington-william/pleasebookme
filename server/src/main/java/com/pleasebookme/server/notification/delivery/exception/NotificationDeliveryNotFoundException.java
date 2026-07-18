package com.pleasebookme.server.notification.delivery.exception;

public class NotificationDeliveryNotFoundException extends RuntimeException {
    public NotificationDeliveryNotFoundException(String message) {
        super(message);
    }
}
