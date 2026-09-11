# What It Is

`AuthorizationContext` is the complete authorization question, assembled once by whoever wants an answer and then passed unchanged through the entire engine described in **[[Security/Authorization/Authorization Engine]]**. Every `AuthorizationPolicy` sees the same instance; nothing rebuilds or mutates it mid-evaluation. If a policy needs a piece of information to make its decision, that information is either already on this record, or the policy abstains — there is no side channel a policy can reach for instead.

Package: `com.pleasebookme.server.security.authorization.context`.

```java
public record AuthorizationContext(
    AuthenticatedPrincipal principal,
    String resourceType,
    String action,
    Object resource,
    ResourceScope scope,
    MembershipSnapshot membership,
    Map<String, Object> attributes
) {
    public AuthorizationContext {
        if (principal == null) {
            throw new IllegalArgumentException("Principal is required");
        }
        if (resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("Resource type is required");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action is required");
        }

        scope = scope == null ? ResourceScope.unscoped() : scope;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
    // ...
}
```

# Fields

| Field | Type | Notes |
| --- | --- | --- |
| `principal` | `AuthenticatedPrincipal` | *Who* is asking. Required — the compact constructor throws if it's `null`. See **[[AuthenticatedPrincipal]]**. |
| `resourceType` | `String` | *What kind of thing* this concerns — e.g. `"BOOKING"`. Required, non-blank. This is the exact string `AuthorizationPolicyRegistry` indexes policies by; see **[[AuthorizationPolicyRegistry]]**. |
| `action` | `String` | *What's being attempted* — e.g. `"CREATE"`. Required, non-blank. |
| `resource` | `Object` | The actual entity instance, when one exists — `null` for a `CREATE` where nothing's been persisted yet. Deliberately untyped; see `resourceAs(...)` below for how a policy narrows it safely. |
| `scope` | `ResourceScope` | Which organization/tenant/owner the resource belongs to. **Never actually `null`** at read time — see the defaulting behavior below. |
| `membership` | `MembershipSnapshot` | The calling user's roles/permissions *inside the one organization this request concerns* — separate from `principal`'s platform-wide roles/permissions. Genuinely nullable; see `hasMembership()`. |
| `attributes` | `Map<String, Object>` | A free-form extension point for anything that doesn't fit the fields above. **Never actually `null`** at read time — same defaulting treatment as `scope`. |

# The Compact Constructor Does Two Different Kinds of Work

Records in Java get a compact constructor for validation, and this one does two distinct things with it — worth separating, because they solve different problems:

**Fail fast on the three fields with no sensible default.** A context with no principal, no resource type, or no action isn't a context with an unusual value — it's not a real question at all, so these three throw `IllegalArgumentException` immediately rather than let a malformed context travel deeper into the engine and fail confusingly inside some unrelated policy.

**Quietly default the two fields that have a safe empty value.** `scope` becomes `ResourceScope.unscoped()` and `attributes` becomes `Map.of()` whenever a caller passes `null` for either. This is what lets every policy downstream treat `context.scope()` and `context.attributes()` as always-present, never-null — nobody has to write a defensive null-check before calling `scope.isUnscoped()` or `attributes.get(key)`. The record absorbs that null-handling once, in one place, instead of every consumer repeating it.

`attributes` is also defensively copied (`Map.copyOf(attributes)`), not just null-checked — once a context exists, nothing can mutate its attribute map out from under a policy that's mid-evaluation, even if the caller who built it kept a reference to the original map and changed it afterward.

# Two Static Factories, Not One Generic Constructor

```java
public static AuthorizationContext forCreate(
    AuthenticatedPrincipal principal,
    String resourceType,
    ResourceScope scope,
    MembershipSnapshot membership
) {
    return new AuthorizationContext(principal, resourceType, "CREATE", null, scope, membership, null);
}

public static AuthorizationContext forResource(
    AuthenticatedPrincipal principal,
    String resourceType,
    String action,
    Object resource,
    ResourceScope scope,
    MembershipSnapshot membership
) {
    return new AuthorizationContext(principal, resourceType, action, resource, scope, membership, null);
}
```

These exist because "the thing being authorized" genuinely comes in two shapes. `forCreate` is for the one action that structurally can never have an existing resource instance to point at — you can't hand a policy the entity being created before it's been created — so this factory hardcodes `action = "CREATE"` and `resource = null` rather than making every caller remember to pass `null` themselves. `forResource` is the general shape, for every action performed against something that already exists (`READ`, `UPDATE`, `DELETE`, and any domain-specific action like `BOOKING.CONFIRM`). Nothing stops a caller from using the full canonical constructor directly instead, but these two factories are the shapes that actually occur in practice, named for what they mean rather than left as a bare six/seven-argument constructor call at every call site.

# `permissionSlug()`

```java
public String permissionSlug() {
    return resourceType + "." + action;
}
```

This is the bridge between the context and the permission vocabulary the rest of the platform already speaks: `auth.permissions` rows are seeded as exactly this shape, `<RESOURCE>.<ACTION>` (`BOOKING.CREATE`, `SERVICE.READ`, and so on — see `AGENTS.md`'s permission-seed notes). `AuthorizationPolicy.evaluatePermission()` (the default method most resource-specific policies lean on) calls this once and checks the result against both `UserPrincipal.hasPermission(...)` and `MembershipSnapshot.hasPermission(...)` — see **[[Security/Authorization/Authorization Engine]]** for that reconciliation in full.

# `hasMembership()`

```java
public boolean hasMembership() {
    return membership != null;
}
```

A plain null-check, given a name — but the name is what matters here. `OrganizationIsolationPolicy` reads `context.hasMembership()` rather than `context.membership() != null` directly, which makes the policy's own logic read as a statement about the domain ("does this user have a membership to check?") instead of a raw null-guard. `membership` is the one field on this record that's allowed to be genuinely absent rather than defaulted to an empty placeholder — unlike `scope`, there's no meaningful "empty" `MembershipSnapshot` to fall back to; either the caller resolved one for this organization, or they didn't.

# `attribute(key)` and `resourceAs(type)`

```java
public Optional<Object> attribute(String key) {
    return Optional.ofNullable(attributes.get(key));
}

public <T> Optional<T> resourceAs(Class<T> type) {
    return type.isInstance(resource) ? Optional.of(type.cast(resource)) : Optional.empty();
}
```

Both are the same idea applied to two different fields: give a policy a way to reach for something that might not be there, or might not be the type it expected, without an unchecked cast or a `NullPointerException` waiting to happen. `attribute(key)` reads out of the free-form extension map. `resourceAs(type)` is the safe way a policy narrows the untyped `resource` field back to something concrete — a `BookingEntity`-specific policy calls `context.resourceAs(BookingEntity.class)` rather than casting `resource` directly, so a context accidentally built around the wrong resource type produces an empty `Optional` instead of a `ClassCastException` at some unrelated line deep inside policy logic.

# Where a Context Actually Gets Built Today

Right now there's exactly one production call site: `AuthorizationPermissionEvaluator`, the bridge that lets `@PreAuthorize("hasPermission(...)")` reach the engine at all. It builds a context directly off the canonical constructor — not either factory — and, notably, always passes `ResourceScope.unscoped()` and `null` for `membership`:

```java
AuthorizationContext context = new AuthorizationContext(
    principal,
    resourceType,
    action,
    resource,
    ResourceScope.unscoped(),
    null,
    null
);
```

This is the wiring gap already flagged in **[[Security/Authorization/Authorization Engine]]**: nothing yet resolves a real `ResourceScope`/`MembershipSnapshot` before handing a context to the engine through this path, which means any scoped policy reached this way can only ever see an unscoped, membership-less context. A future caller that wants scoped enforcement has two options: resolve `scope`/`membership` itself and build a context via `forResource(...)`/`forCreate(...)` before calling `AuthorizationService.require(...)` directly, or wait for `AuthorizationPermissionEvaluator` itself to be extended to call `ScopeResolverRegistry`/`MembershipResolver` before building its context.

# See Also

- **[[Security/Authorization/Authorization Engine]]** — the full engine this context is the input to, including the decision algebra that consumes it.
- **[[AuthorizationDecision]]** — what a policy hands back after looking at one of these.
- **[[AuthorizationPolicyRegistry]]** — how `resourceType`/`action` on this record select which policies even get a chance to look at it.
- **[[AuthenticatedPrincipal]]** — the `principal` field, and why it alone doesn't carry organization/tenant data (the reason `scope`/`membership` exist as separate fields here at all).
