# What It Does

The entire capability model for a widget, today: a fixed allow-list of permission slugs a `WidgetPrincipal` may ever perform. Registered against `"*"` at `order = -10`, right after `ActorStatusPolicy`.

```java
private static final Set<String> ALLOWED_SLUGS = Set.of(
    "AVAILABILITY.READ", "SERVICE.READ",
    "SELECTEDSLOT.CREATE", "SELECTEDSLOT.DELETE",
    "BOOKING.CREATE", "ATTENDEE.CREATE"
);

@Override
public AuthorizationDecision evaluate(AuthorizationContext context) {
    if (!(context.principal() instanceof WidgetPrincipal)) {
        return AuthorizationDecision.abstain(policyName());
    }
    if (ALLOWED_SLUGS.contains(context.permissionSlug())) {
        return AuthorizationDecision.permit(policyName());
    }
    return AuthorizationDecision.deny(policyName(), "WIDGET_CAPABILITY_DENIED",
        "Widgets are not permitted to perform " + context.permissionSlug());
}
```

# Why It Exists

Widgets have no roles or permissions on their principal (see **[[AuthenticatedPrincipal]]** — `WidgetPrincipal` is deliberately minimal), so there's nothing for `evaluatePermission()`'s usual `hasPermission(...)` check to consult. This policy stands in for that entirely: it's the only place in the engine that both grants (`PERMIT` on an allow-listed slug) and denies for a widget, in one policy, rather than splitting the veto and the grant across two. Abstains immediately for any non-widget actor, so it never affects User-driven requests.

This allow-list is explicitly a placeholder for a real capability model, not the finished design — see **[[Authorization Engine]]** for how it fits into the wider decision algebra.
