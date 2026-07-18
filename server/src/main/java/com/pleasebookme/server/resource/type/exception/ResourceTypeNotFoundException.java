package com.pleasebookme.server.resource.type.exception;

public class ResourceTypeNotFoundException extends RuntimeException {
    public ResourceTypeNotFoundException(String message) {
        super(message);
    }
}
