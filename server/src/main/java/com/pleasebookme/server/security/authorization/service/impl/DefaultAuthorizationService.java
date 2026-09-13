package com.pleasebookme.server.security.authorization.service.impl;

import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;
import com.pleasebookme.server.security.authorization.decision.DecisionEffect;
import com.pleasebookme.server.security.authorization.exception.AuthorizationDeniedException;
import com.pleasebookme.server.security.authorization.exception.PolicyEvaluationException;
import com.pleasebookme.server.security.authorization.policy.AuthorizationPolicy;
import com.pleasebookme.server.security.authorization.registry.AuthorizationPolicyRegistry;
import com.pleasebookme.server.security.authorization.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultAuthorizationService implements AuthorizationService {

    private final AuthorizationPolicyRegistry policyRegistry;

    private static final String NO_POLICY_POLICY_NAME = "AuthorizationService";

    @Override
    public AuthorizationDecision authorize(AuthorizationContext context) {
        List<AuthorizationPolicy> policies = policyRegistry.resolve(
            context.resourceType(),
            context.action()
        );

        AuthorizationDecision permitted = null;

        for (AuthorizationPolicy policy : policies) {
            AuthorizationDecision decision = evaluate(policy, context);

            if (decision.effect() == DecisionEffect.DENY) {
                return decision;
            }

            if (decision.effect() == DecisionEffect.PERMIT && permitted == null) {
                permitted = decision;
            }
        }

        return permitted != null
            ? permitted
            : AuthorizationDecision.deny(
                NO_POLICY_POLICY_NAME,
                "NO_POLICY",
                "No policy permitted " + context.action() + " on " + context.resourceType()
            );
    }

    @Override
    public void require(AuthorizationContext context) {
        AuthorizationDecision decision = authorize(context);

        if (!decision.granted()) {
            throw new AuthorizationDeniedException(decision);
        }
    }

    private AuthorizationDecision evaluate(AuthorizationPolicy policy, AuthorizationContext context) {
        try {
            return policy.evaluate(context);
        } catch (RuntimeException exception) {
            throw new PolicyEvaluationException(
                "Policy " + policy.getClass().getSimpleName()
                    + " failed to evaluate " + context.permissionSlug(),
                exception
            );
        }
    }
}
