package com.pleasebookme.server.core.booking.authorization;

import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;
import com.pleasebookme.server.security.authorization.policy.AuthorizationPolicy;
import org.springframework.stereotype.Component;

@Component
public class BookingCancelPolicy implements AuthorizationPolicy {

    @Override
    public String resourceType() {
        return "BOOKING";
    }

    @Override
    public boolean supports(String action) {
        return "CANCEL".equals(action);
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public AuthorizationDecision evaluate(AuthorizationContext context) {
        return evaluatePermission(context);
    }
}
