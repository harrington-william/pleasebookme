package com.pleasebookme.server.security.authorization.policy;

import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;

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
}
