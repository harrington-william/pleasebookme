package com.pleasebookme.server.security.authorization.policy.impl;

import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;
import com.pleasebookme.server.security.authorization.policy.AuthorizationPolicy;
import com.pleasebookme.server.security.identity.principal.WidgetPrincipal;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class WidgetCapabilityPolicy implements AuthorizationPolicy {

    private static final Set<String> ALLOWED_SLUGS = Set.of(
        "AVAILABILITY.READ",
        "SERVICE.READ",
        "SELECTEDSLOT.CREATE",
        "SELECTEDSLOT.DELETE",
        "BOOKING.CREATE",
        "ATTENDEE.CREATE"
    );

    @Override
    public String resourceType() {
        return WILDCARD_RESOURCE_TYPE;
    }

    @Override
    public int order() {
        return -10;
    }

    @Override
    public AuthorizationDecision evaluate(AuthorizationContext context) {
        if (!(context.principal() instanceof WidgetPrincipal)) {
            return AuthorizationDecision.abstain(policyName());
        }

        if (ALLOWED_SLUGS.contains(context.permissionSlug())) {
            return AuthorizationDecision.permit(policyName());
        }

        return AuthorizationDecision.deny(
            policyName(),
            "WIDGET_CAPABILITY_DENIED",
            "Widgets are not permitted to perform " + context.permissionSlug()
        );
    }

    private String policyName() {
        return getClass().getSimpleName();
    }
}
