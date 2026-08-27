# What It Does

The widget equivalent of `OrganizationIsolationPolicy` — keeps a `WidgetPrincipal` confined to the one tenant it belongs to. Registered against `"*"` at `order = 0`.

```java
@Override
public AuthorizationDecision evaluate(AuthorizationContext context) {
    if (!(context.principal() instanceof WidgetPrincipal widgetPrincipal)) {
        return AuthorizationDecision.abstain(policyName());
    }

    ResourceScope scope = context.scope();
    if (scope.isUnscoped()) {
        return AuthorizationDecision.deny(policyName(), "OUT_OF_SCOPE",
            "Resource " + context.resourceType() + " has no resolvable tenant scope");
    }
    if (!scope.matchesTenant(widgetPrincipal.tenantId())) {
        return AuthorizationDecision.deny(policyName(), "OUT_OF_SCOPE",
            "Widget tenant does not match the resource's tenant");
    }
    return AuthorizationDecision.abstain(policyName());
}
```

# Why It Exists

A pure veto, never a grant. Widgets are always bound to exactly one tenant (see **[[AuthenticatedPrincipal]]** — a `WidgetPrincipal`'s `tenantUid`/`tenantId` are never null), so unlike `OrganizationIsolationPolicy` there's no platform-wide-role bypass to check — a widget is never platform-wide by definition, so every widget request goes through this check unconditionally.

Note it compares against `widgetPrincipal.tenantId()` — the `BigInteger` surrogate PK, not `tenantUid()` (`UUID`) — because `ResourceScope.tenantId()` is itself keyed by the DB-native id, resolved by walking JPA entity graphs rather than external identifiers. See **[[AuthenticatedPrincipal]]** for why `WidgetPrincipal` carries both id forms.

See **[[Authorization Engine]]** for the full decision algebra this fits into.
