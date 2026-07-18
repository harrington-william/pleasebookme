package com.pleasebookme.server.notification.template.exception;

public class NotificationTemplateNotFoundException extends RuntimeException {
    public NotificationTemplateNotFoundException(String message) {
        super(message);
    }
}
