package com.pleasebookme.server.resource.overrides.exception;

public class ResourceOverrideNotFoundException extends RuntimeException {
    public ResourceOverrideNotFoundException(String message) {
        super(message);
    }
}
