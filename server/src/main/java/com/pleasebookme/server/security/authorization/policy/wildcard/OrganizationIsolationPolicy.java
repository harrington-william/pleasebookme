package com.pleasebookme.server.security.authorization.policy.wildcard;

import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;
import com.pleasebookme.server.security.authorization.policy.AuthorizationPolicy;
import com.pleasebookme.server.security.authorization.scope.ResourceScope;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OrganizationIsolationPolicy implements AuthorizationPolicy {

    private static final Set<String> PLATFORM_WIDE_ROLES = Set.of("PLATFORM_OWNER", "PLATFORM_MANAGER");

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
        if (!(context.principal() instanceof UserPrincipal userPrincipal)) {
            return AuthorizationDecision.abstain(policyName());
        }

        if (PLATFORM_WIDE_ROLES.stream().anyMatch(userPrincipal::hasRole)) {
            return AuthorizationDecision.abstain(policyName());
        }

        ResourceScope scope = context.scope();

        if (scope.isUnscoped()) {
            return AuthorizationDecision.deny(
                policyName(),
                "OUT_OF_SCOPE",
                "Resource " + context.resourceType() + " has no resolvable organization scope"
            );
        }

        if (!context.hasMembership() || !scope.matchesOrganization(context.membership().organizationId())) {
            return AuthorizationDecision.deny(
                policyName(),
                "OUT_OF_SCOPE",
                "User has no accepted membership in the resource's organization"
            );
        }

        return AuthorizationDecision.abstain(policyName());
    }

    private String policyName() {
        return getClass().getSimpleName();
    }
}
