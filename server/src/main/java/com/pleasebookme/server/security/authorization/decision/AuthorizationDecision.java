package com.pleasebookme.server.security.authorization.decision;

public record AuthorizationDecision(
    DecisionEffect effect,
    String code,
    String reason,
    String policyName
) {
    public AuthorizationDecision {
        if (effect == null) {
            throw new IllegalArgumentException("Decision effect is required");
        }
    }

    public boolean granted() {
        return effect == DecisionEffect.PERMIT;
    }

    public static AuthorizationDecision permit(String policyName) {
        return new AuthorizationDecision(
            DecisionEffect.PERMIT,
            "PERMIT",
            "Access granted",
            policyName
        );
    }

    public static AuthorizationDecision deny(
        String policyName,
        String code,
        String reason
    ) {
        return new AuthorizationDecision(
            DecisionEffect.DENY,
            code,
            reason,
            policyName
        );
    }

    public static AuthorizationDecision abstain(String policyName) {
        return new AuthorizationDecision(
            DecisionEffect.ABSTAIN,
            "NO_POLICY",
            "No policy expressed an opinion for this resource type and action",
            policyName
        );
    }
}
