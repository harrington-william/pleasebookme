package com.pleasebookme.server.security.authorization.policy;

import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;

public interface AuthorizationPolicy {

    String WILDCARD_RESOURCE_TYPE = "*";

    String resourceType();

    default boolean supports(String action) {
        return true;
    }

    default int order() {
        return 0;
    }

    AuthorizationDecision evaluate(AuthorizationContext context);

    default AuthorizationDecision evaluatePermission(AuthorizationContext context) {
        if (!(context.principal() instanceof UserPrincipal userPrincipal)) {
            return AuthorizationDecision.abstain(getClass().getSimpleName());
        }

        String slug = context.permissionSlug();

        boolean granted = userPrincipal.hasPermission(slug)
            || (context.hasMembership() && context.membership().hasPermission(slug));

        if (!granted) {
            return AuthorizationDecision.deny(
                getClass().getSimpleName(),
                "MISSING_PERMISSION",
                "Neither platform roles nor the resolved membership grant " + slug
            );
        }

        return AuthorizationDecision.permit(getClass().getSimpleName());
    }
}
