package com.pleasebookme.server.tenant.plan.exception;

public class TenantPlanNotFoundException extends RuntimeException {
    public TenantPlanNotFoundException(String message) {
        super(message);
    }
}
