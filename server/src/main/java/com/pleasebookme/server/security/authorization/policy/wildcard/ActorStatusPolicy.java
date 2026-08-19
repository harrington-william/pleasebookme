package com.pleasebookme.server.security.authorization.policy.wildcard;

import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;
import com.pleasebookme.server.security.authorization.policy.AuthorizationPolicy;
import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.identity.principal.WidgetPrincipal;
import org.springframework.stereotype.Component;

@Component
public class ActorStatusPolicy implements AuthorizationPolicy {

    @Override
    public String resourceType() {
        return WILDCARD_RESOURCE_TYPE;
    }

    @Override
    public int order() {
        return -20;
    }

    @Override
    public AuthorizationDecision evaluate(AuthorizationContext context) {
        if (!isActive(context.principal())) {
            return AuthorizationDecision.deny(
                policyName(),
                "ACTOR_INACTIVE",
                "Actor " + context.principal().subject() + " is not in an active state"
            );
        }

        return AuthorizationDecision.abstain(policyName());
    }

    private boolean isActive(AuthenticatedPrincipal principal) {
        return switch (principal) {
            case UserPrincipal userPrincipal -> userPrincipal.isActive();
            case WidgetPrincipal widgetPrincipal -> widgetPrincipal.isActive();
        };
    }

    private String policyName() {
        return getClass().getSimpleName();
    }
}
