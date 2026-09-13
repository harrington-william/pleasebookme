package com.pleasebookme.server.security.authorization.exception;

import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;

public class AuthorizationDeniedException extends RuntimeException {

    private final AuthorizationDecision decision;

    public AuthorizationDeniedException(AuthorizationDecision decision) {
        super(decision.reason());
        this.decision = decision;
    }

    public AuthorizationDecision decision() {
        return decision;
    }
}
