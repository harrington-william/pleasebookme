package com.pleasebookme.server.security.authorization.policy.wildcard;

import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;
import com.pleasebookme.server.security.authorization.policy.AuthorizationPolicy;
import com.pleasebookme.server.security.authorization.scope.ResourceScope;
import com.pleasebookme.server.security.identity.principal.WidgetPrincipal;
import org.springframework.stereotype.Component;

@Component
public class WidgetTenantIsolationPolicy implements AuthorizationPolicy {

    @Override
    public String resourceType() {
        return WILDCARD_RESOURCE_TYPE;
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public AuthorizationDecision evaluate(AuthorizationContext context) {
        if (!(context.principal() instanceof WidgetPrincipal widgetPrincipal)) {
            return AuthorizationDecision.abstain(policyName());
        }

        ResourceScope scope = context.scope();

        if (scope.isUnscoped()) {
            return AuthorizationDecision.deny(
                policyName(),
                "OUT_OF_SCOPE",
                "Resource " + context.resourceType() + " has no resolvable tenant scope"
            );
        }

        if (!scope.matchesTenant(widgetPrincipal.tenantId())) {
            return AuthorizationDecision.deny(
                policyName(),
                "OUT_OF_SCOPE",
                "Widget tenant does not match the resource's tenant"
            );
        }

        return AuthorizationDecision.abstain(policyName());
    }

    private String policyName() {
        return getClass().getSimpleName();
    }
}
