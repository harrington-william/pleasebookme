package com.pleasebookme.server.integration.oauthconnection.exception;

public class DuplicateOAuthConnectionException extends RuntimeException {
    public DuplicateOAuthConnectionException(String message) {
        super(message);
    }
}
