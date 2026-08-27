# What It Does

Keeps a `UserPrincipal` confined to organizations they actually belong to. Registered against `"*"` at `order = 0`.

```java
private static final Set<String> PLATFORM_WIDE_ROLES = Set.of("PLATFORM_OWNER", "PLATFORM_MANAGER");

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
        return AuthorizationDecision.deny(policyName(), "OUT_OF_SCOPE", "...");
    }
    if (!context.hasMembership() || !scope.matchesOrganization(context.membership().organizationId())) {
        return AuthorizationDecision.deny(policyName(), "OUT_OF_SCOPE", "...");
    }
    return AuthorizationDecision.abstain(policyName());
}
```

# Why It Exists

A pure veto, like `ActorStatusPolicy` — it never grants, only narrows. Since `UserPrincipal` deliberately carries no organization data (see **[[AuthenticatedPrincipal]]**), this is the one policy that actually enforces "you must hold an accepted membership in the resource's own organization" — checking the resolved `ResourceScope` against the resolved `MembershipSnapshot` (see **[[AuthorizationContext]]**), never against anything cached on the principal itself.

`PLATFORM_OWNER`/`PLATFORM_MANAGER` — the system-level roles from **[[Role Design]]** — bypass this check entirely, since those roles are meant to act across every organization by definition. Everyone else abstains only after passing the scope/membership check; the actual grant still has to come from a resource-specific policy's `evaluatePermission()` call.

See **[[Authorization Engine]]** for the full decision algebra this fits into.
