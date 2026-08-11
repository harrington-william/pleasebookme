package com.pleasebookme.server.security.oauth.google.exception;

import lombok.Getter;

@Getter
public class GoogleTokenRefreshException extends RuntimeException {
    // Google answers invalid_grant when the user revoked access, changed their
    // password, or the refresh token expired. That is terminal for the
    // connection, unlike a transient 5xx which is worth retrying.
    private final boolean invalidGrant;

    public GoogleTokenRefreshException(
        String message,
        boolean invalidGrant
    ) {
        super(message);
        this.invalidGrant = invalidGrant;
    }
}
