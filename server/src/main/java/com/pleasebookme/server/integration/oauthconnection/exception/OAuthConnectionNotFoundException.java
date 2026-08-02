package com.pleasebookme.server.integration.oauthconnection.exception;

public class OAuthConnectionNotFoundException extends RuntimeException {
    public OAuthConnectionNotFoundException(String message) {
        super(message);
    }
}
