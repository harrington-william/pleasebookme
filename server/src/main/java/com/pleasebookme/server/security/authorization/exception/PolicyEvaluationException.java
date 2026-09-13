package com.pleasebookme.server.security.authorization.exception;

public class PolicyEvaluationException extends RuntimeException {
    public PolicyEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}
