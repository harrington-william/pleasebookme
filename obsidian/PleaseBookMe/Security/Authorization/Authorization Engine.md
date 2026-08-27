# Purpose

Authentication answers *"who is this actor?"* — that question belongs to the Identity subsystem (see **[[AuthenticatedPrincipal]]**). This engine answers a different question entirely: *"is this actor allowed to perform this action, on this resource, in this scope?"* The two are deliberately separate. Authentication produces an `AuthenticatedPrincipal`; this engine only ever consumes one — it never re-derives identity, never talks to Spring Security's `SecurityContextHolder` directly, and never appears anywhere in the authentication pipeline.

# The Shape of the Engine

A registry of small, independent rules (`AuthorizationPolicy` beans) each look at one request and vote **PERMIT**, **DENY**, or **ABSTAIN**; one fixed rule (deny overrides, default deny) turns however many votes came back into a single yes/no answer. New authorization logic is added by registering a new policy bean — never by editing an existing one, and never by adding an `if` statement inside a controller or service.

# End-to-End Flow

```
Controller method annotated @PreAuthorize("hasPermission(#id, 'BOOKING', 'CREATE')")
        │
        ▼
AuthorizationPermissionEvaluator.hasPermission(...)
        │
        ├── CurrentPrincipalProvider.find()        → who is calling
        └── builds an AuthorizationContext          → principal, resourceType, action, resource, scope, membership
        │
        ▼
AuthorizationService.authorize(context)
        │
        ▼
AuthorizationPolicyRegistry.resolve(resourceType, action)
        │
        └── every "*" (wildcard) policy + every policy registered for this resourceType,
            filtered to policy.supports(action), sorted by policy.order()
        │
        ▼
for each resolved AuthorizationPolicy, in order:
        policy.evaluate(context)  →  PERMIT | DENY | ABSTAIN
        │
        ├── any DENY  → stop immediately, the whole call is denied
        └── first PERMIT seen is remembered, evaluation continues (a later DENY can still override it)
        │
        ▼
AuthorizationDecision
        │
        ├── granted()  → true only if the final result is PERMIT
        └── nothing permitted at all → default DENY ("NO_POLICY")
        │
        ▼
true/false back to Spring's @PreAuthorize        (or AuthorizationDeniedException from .require(...))
```

Every box above is one Java class or interface. The table below is the map; the sections after it are the "why."

# Component Map

| Component                                                                  | Package                     | Role                                                                                                                                                                                                                          |
| -------------------------------------------------------------------------- | --------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **[[AuthorizationContext]]**                                               | `authorization/context/`    | The complete question, assembled by the caller: `principal`, `resourceType`, `action`, `resource`, `scope`, `membership`, `attributes`. Also derives `permissionSlug()` → `"<resourceType>.<action>"`, e.g. `BOOKING.CREATE`. |
| `ResourceScope`                                                            | `authorization/scope/`      | Which organization/tenant/owner a resource belongs to (`organizationId`, `tenantId`, `ownerUserId`) — entity-agnostic, so policies never need to know a resource's concrete type.                                             |
| `ScopeResolver<T>` / `ScopeResolverRegistry`                               | `authorization/scope/`      | Maps one entity type to a `ResourceScope` (e.g. `BookingScopeResolver` walks `Booking → Service → Organization → Tenant`). The registry dispatches to whichever resolver matches the resource's runtime type.                 |
| `MembershipSnapshot`                                                       | `authorization/membership/` | A user's roles/permissions **inside one specific organization** — `membershipId`, `organizationId`, `roles`, `permissions`.                                                                                                   |
| `MembershipResolver`                                                       | `authorization/membership/` | Resolves a `MembershipSnapshot` for a `(userUid, organizationId)` pair from `organization.memberships` + `organization.membership_roles`.                                                                                     |
| `AuthorizationPolicy`                                                      | `authorization/policy/`     | The unit of rule: `resourceType()`, `supports(action)`, `order()`, `evaluate(context)`. Also carries a default `evaluatePermission()` method every policy can reuse.                                                          |
| `AuthorizationPolicy.WILDCARD_RESOURCE_TYPE` (`"*"`)                       | `authorization/policy/`     | A policy registered against this constant runs for *every* resource type and action — used for cross-cutting rules (actor status, tenant isolation) rather than one specific domain.                                          |
| **[[AuthorizationDecision]]**                                              | `authorization/decision/`   | The outcome of one policy's evaluation *or* of the whole engine: `effect` (`PERMIT`/`DENY`/`ABSTAIN`), `code`, `reason`, `policyName`. `granted()` is `true` only for `PERMIT`.                                               |
| **[[AuthorizationPolicyRegistry]]** / `DefaultAuthorizationPolicyRegistry` | `authorization/registry/`   | Indexes every policy bean by `resourceType()` at startup; separately holds every wildcard policy. `resolve(resourceType, action)` returns the combined, action-filtered, order-sorted list.                                   |
| `AuthorizationService` / `DefaultAuthorizationService`                     | `authorization/service/`    | Runs a resolved policy list through the decision algebra. `authorize(context)` returns a `AuthorizationDecision`; `require(context)` throws if it isn't granted.                                                              |
| `AuthorizationPermissionEvaluator`                                         | `authorization/adapter/`    | Implements Spring Security's `PermissionEvaluator`. This is what makes `@PreAuthorize("hasPermission(...)")` actually call into the engine.                                                                                   |

# The Decision Algebra

This is the one rule the entire engine runs on, and it's worth stating precisely because it's easy to get subtly wrong when reading the code casually:

1. Policies are evaluated **in order** (`order()`, ascending).
2. The moment any policy returns **DENY**, evaluation stops and the whole call is denied — a deny from *any* policy overrides everything, regardless of what ran before or would have run after.
3. The **first PERMIT** encountered is remembered, but evaluation keeps going — a later policy can still deny it.
4. If evaluation finishes with nothing having denied and nothing having permitted (every policy abstained, or no policy matched the resource type/action at all), the result is a default **deny**, coded `NO_POLICY`.

In plain terms: **deny-overrides, default-deny.** Permission has to be actively granted by at least one policy; silence from every policy is treated as "no," never as "sure, why not." Full field-by-field detail on the decision object itself — including a genuine gotcha around its `code` field — is its own page: **[[AuthorizationDecision]]**.

`AuthorizationService.authorize(context)` returns this outcome as an `AuthorizationDecision`. `require(context)` is the same call with a throw attached — `AuthorizationDeniedException` if `decision.granted()` is `false`. A policy that throws a `RuntimeException` mid-`evaluate()` doesn't propagate raw; it's caught and rewrapped as `PolicyEvaluationException`, naming which policy misbehaved — so a bug in one policy is loud and traceable, never a silent authorization bypass.

# Wildcard Policies vs. Resource-Specific Policies

Every policy declares a `resourceType()`. Most will eventually declare a specific one — `"BOOKING"`, `"SERVICE"`, and so on — and only run when a request concerns that exact resource type. A policy can instead declare the constant `AuthorizationPolicy.WILDCARD_RESOURCE_TYPE` (`"*"`), which means it runs for **every** resource type and action, no matter what. `AuthorizationPolicyRegistry.resolve(resourceType, action)` always returns the full set of matching wildcard policies **plus** whatever's registered for the specific resource type, combined and sorted by `order()` together.

Wildcards are for cross-cutting concerns that don't belong to any one domain — "is this actor even active," "does this widget's tenant match the resource's tenant" — while resource-specific policies are where the actual per-domain permission grant lives (see `evaluatePermission()` below). How the registry actually builds and combines these two buckets — including a subtle double-counting guard and how ties in `order()` are handled — is covered in full on **[[AuthorizationPolicyRegistry]]**.

# The Four Built-In Wildcard Policies

These four ship registered against `"*"`, so every authorization call passes through all of them:

| Policy | Order | What it does |
|---|---|---|
| **[[ActorStatusPolicy]]** | −20 | Denies if the actor (User or Widget) isn't in an active status. Otherwise abstains — it only ever vetoes, never grants. |
| **[[WidgetCapabilityPolicy]]** | −10 | For a `WidgetPrincipal`: permits only if the requested `<RESOURCE>.<ACTION>` slug is on a fixed allow-list (`AVAILABILITY.READ`, `SERVICE.READ`, `SELECTEDSLOT.CREATE`/`DELETE`, `BOOKING.CREATE`, `ATTENDEE.CREATE`); denies everything else. Abstains for any other actor type. |
| **[[OrganizationIsolationPolicy]]** | 0 | For a `UserPrincipal` without a platform-wide role (`PLATFORM_OWNER`/`PLATFORM_MANAGER`): denies if the resource has no resolvable organization scope, or if the user holds no accepted membership in that organization. Otherwise abstains — the actual grant still has to come from somewhere else. |
| **[[WidgetTenantIsolationPolicy]]** | 0 | For a `WidgetPrincipal`: denies if the resource's tenant scope doesn't match the widget's own tenant. Otherwise abstains. |

The ordering is deliberate: the cheap, actor-type vetoes (`−20`, `−10`) run before the scope/membership reasoning (`0`), so an inactive actor or an out-of-allow-list widget call is rejected before any database-backed scope check even happens.

None of these four ever *grants* anything by themselves except `WidgetCapabilityPolicy`'s allow-list match — they exist to narrow and veto. The actual "does this user's role let them do this" grant comes from `AuthorizationPolicy.evaluatePermission()`, a default method every policy inherits:

```java
default AuthorizationDecision evaluatePermission(AuthorizationContext context) {
    if (!(context.principal() instanceof UserPrincipal userPrincipal)) {
        return AuthorizationDecision.abstain(getClass().getSimpleName());
    }

    boolean granted = userPrincipal.hasPermission(context.permissionSlug())
        || (context.hasMembership() && context.membership().hasPermission(context.permissionSlug()));

    return granted
        ? AuthorizationDecision.permit(getClass().getSimpleName())
        : AuthorizationDecision.deny(getClass().getSimpleName(), "MISSING_PERMISSION", "...");
}
```

A resource-specific policy calls this to check the permission slug against **either** the user's platform-wide permissions (`UserPrincipal.permissions`) **or** their organization-scoped permissions (`MembershipSnapshot.permissions`) — whichever one actually has the slug. This is the one place the two very different sources of "what can this user do" (platform-wide roles vs. per-organization membership roles) get reconciled into a single yes/no.

# Why `ResourceScope` and `MembershipSnapshot` Exist as Separate Objects

`UserPrincipal` deliberately carries no organization or tenant information — see **[[AuthenticatedPrincipal]]** for the full reasoning, but the short version is that a user can belong to more than one organization, so there's no single tenant to bind at authentication time. That means every authorization decision that cares about "which organization does this concern, and what can this user do *there*" has to resolve that information fresh, per request, per resource — which is exactly what these two objects are for:

- **`ResourceScope`** answers *"which organization/tenant does this resource belong to?"* — resolved from the resource itself via `ScopeResolver<T>`, entirely independent of who's asking.
- **`MembershipSnapshot`** answers *"what can this specific user do inside that specific organization?"* — resolved from `(userUid, organizationId)` via `MembershipResolver`, entirely independent of which resource is involved.

Neither object is ever cached on the principal, and neither is meant to be — both are resolved at the moment they're needed, against current data, for exactly the one organization the current request concerns. A user who's an `OWNER` in one organization and has no membership at all in another gets correctly different answers for the same permission check on two different resources, without the identity layer ever needing to know both organizations at once.

# Spring Integration — How `@PreAuthorize` Reaches the Engine

`AuthorizationPermissionEvaluator` implements Spring Security's `PermissionEvaluator` interface, and `AuthorizationExpressionHandlerConfig` registers it into the `MethodSecurityExpressionHandler` Spring uses to evaluate `@PreAuthorize` SpEL expressions. That's the whole wiring: it's what makes `hasPermission(...)` inside an `@PreAuthorize` annotation resolve to anything at all, rather than being an undefined SpEL function.

The evaluator reads the current principal via `CurrentPrincipalProvider`, splits the permission string into a resource type and action, and calls `AuthorizationService.authorize(...)` with the resulting `AuthorizationContext`.

**Worth knowing before relying on this**: the evaluator currently builds that context with `ResourceScope.unscoped()` and no `MembershipSnapshot` — it does not call `ScopeResolverRegistry` or `MembershipResolver` itself. A scoped policy (`OrganizationIsolationPolicy`, `WidgetTenantIsolationPolicy`) reached purely through `@PreAuthorize("hasPermission(...)")` will therefore deny with `OUT_OF_SCOPE` unless the caller holds a platform-wide role. Both resolvers are built and independently tested (`BookingScopeResolver`, `DefaultMembershipResolver`) — this is infrastructure built ahead of its consumer, the same forward-scaffolding pattern documented elsewhere in this codebase (see `AGENTS.md`). Also worth noting: no controller in the codebase uses `@PreAuthorize` yet at all — the engine is complete and unit-tested end to end, but not yet enforced at the controller layer. Wiring scope/membership resolution into the evaluator (or having callers build the full `AuthorizationContext` directly and call `AuthorizationService.require(...)` themselves) is the remaining step before `@PreAuthorize` is safe to use broadly.

# See Also

- **[[Security Architecture]]** §8 — this engine's place inside the platform's overall security architecture, alongside Identity, Token & Session, and Credential Encryption.
- **[[AuthenticatedPrincipal]]** — why `UserPrincipal` carries no organization/tenant data, which is the entire reason `ResourceScope`/`MembershipSnapshot` exist as separate, per-request-resolved objects.
- **[[Role Design]]** — the two-level role model (`SYSTEM`/`BUSINESS`) whose names show up directly in policy logic — `PLATFORM_OWNER`/`PLATFORM_MANAGER` are exactly the system-level roles `OrganizationIsolationPolicy` bypasses on.
- **[[AuthorizationContext]]** — the input every policy evaluates, in full field-by-field detail.
- **[[AuthorizationDecision]]** — the output every policy (and the engine itself) produces, including the `NO_POLICY` code overlap worth knowing about.
- **[[AuthorizationPolicyRegistry]]** — exactly how policy beans become the ordered, resolved list the decision algebra runs over.
