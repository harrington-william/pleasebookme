package com.pleasebookme.server.notification.template.exception;

public class DuplicateNotificationTemplateException extends RuntimeException {
    public DuplicateNotificationTemplateException(String message) {
        super(message);
    }
}
