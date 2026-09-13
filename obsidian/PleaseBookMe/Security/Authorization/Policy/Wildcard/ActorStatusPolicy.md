# What It Does

Denies every request from an actor that isn't currently active — the one check every single authorization call passes through first, regardless of resource type or action. Registered against `"*"` at `order = -20`, the lowest (earliest-running) order of any policy in the engine.

```java
@Override
public AuthorizationDecision evaluate(AuthorizationContext context) {
    if (!isActive(context.principal())) {
        return AuthorizationDecision.deny(policyName(), "ACTOR_INACTIVE",
            "Actor " + context.principal().subject() + " is not in an active state");
    }
    return AuthorizationDecision.abstain(policyName());
}

private boolean isActive(AuthenticatedPrincipal principal) {
    return switch (principal) {
        case UserPrincipal userPrincipal -> userPrincipal.isActive();
        case WidgetPrincipal widgetPrincipal -> widgetPrincipal.isActive();
    };
}
```

# Why It Exists

It's a pure veto, never a grant — an active actor gets `ABSTAIN`, not `PERMIT`, so the actual permission decision always comes from elsewhere. It runs before every other policy specifically because it's the cheapest possible check (no scope lookup, no membership resolution, just a status flag already sitting on the principal) and the one most worth failing fast on — a suspended user or a disabled widget shouldn't burn a database round-trip in `OrganizationIsolationPolicy` or `MembershipResolver` before being told no.

The exhaustive `switch` over the sealed `AuthenticatedPrincipal` (see **[[AuthenticatedPrincipal]]**) is what lets one policy cover both actor types without an `instanceof` chain — `UserPrincipal.isActive()` checks `accountStatus == ACTIVE`, `WidgetPrincipal.isActive()` checks `status == ACTIVE`.

See **[[Security/Authorization/Authorization Engine]]** for how this fits into the full decision algebra.
