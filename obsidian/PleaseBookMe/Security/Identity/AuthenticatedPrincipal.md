# What It Is

`AuthenticatedPrincipal` is the single canonical representation of an authenticated actor in PleaseBookMe. Every authentication mechanism — Username/Password, JWT, Google Sign-In, Widget Token, and future API Keys/Service Accounts — produces one of these, and every authorization decision (see **[[Security Architecture]]**, section 8) consumes one. It belongs entirely to the security domain: it has no knowledge of `UserDetails`, `Authentication`, `GrantedAuthority`, or any other Spring Security type.

Package: `com.pleasebookme.server.security.identity.principal`

```java
public sealed interface AuthenticatedPrincipal
    permits UserPrincipal, WidgetPrincipal {

    AuthenticatedActorType actorType();

    UUID subject();

    UUID tenantUid();
}
```

That is the entire interface — three accessor methods, no default methods, no behavior. Everything an actor actually *is* lives on the concrete record; the interface only guarantees enough for generic, actor-agnostic code (the token layer, the current-principal reader) to do its job without knowing which actor it's holding.

---

# Why Sealed

`sealed ... permits UserPrincipal, WidgetPrincipal` is a Java 21 language feature, not a convention — the compiler enforces it. Two concrete consequences show up directly in this codebase:

1. **No third implementation can exist outside this file's `permits` clause.** Adding a new actor type is a one-line edit to the interface declaration, and every place that pattern-matches over the sealed type immediately becomes a compile error until it's updated to handle the new case.
2. **A `switch` over the sealed type itself needs no `default` branch**, and the compiler rejects the switch if a case is missing. Two real examples:

```java
// AuthenticationTokenFactory.create — exhaustive, no default possible
return switch (principal) {
    case UserPrincipal user -> createUserAuthentication(user);
    case WidgetPrincipal widget -> createWidgetAuthentication(widget);
};
```

```java
// ActorStatusPolicy.isActive — exhaustive, no default possible
return switch (principal) {
    case UserPrincipal userPrincipal -> userPrincipal.isActive();
    case WidgetPrincipal widgetPrincipal -> widgetPrincipal.isActive();
};
```

If a third `AuthenticatedPrincipal` implementation were added tomorrow, both of these would fail to compile until a matching `case` was added — the exhaustiveness check surfaces every call site that needs updating, rather than the new actor type silently falling through to undefined behavior at runtime.

## The contrasting case: `AuthenticatedActorType` is *not* sealed-exhaustive

It is easy to conflate "the sealed principal type" with "the actor-type enum," but they are two different closed sets, and only one of them is currently exhaustive:

```java
public enum AuthenticatedActorType {
    USER,
    SYSTEM,
    WIDGET,
    API_KEY,
    WEBHOOK,
    INTEGRATION
}
```

Six enum constants exist, but only two (`USER`, `WIDGET`) have a matching `AuthenticatedPrincipal` implementation today. `SYSTEM`, `API_KEY`, `WEBHOOK`, and `INTEGRATION` are seeded ahead of their implementation — the same forward-looking-scaffolding pattern the codebase already uses for the `auth.permissions` `SESSION.*` rows and the `api_keys` table (see `AGENTS.md`). Because of this gap, `JwtAuthenticationFilter` switches on the **enum**, not the sealed principal type, and *must* carry a `default`:

```java
AuthenticatedPrincipal principal = switch (claims.actorType()) {
    case USER -> userPrincipalMapper.map(userIdentityLoader.loadByUid(claims.subject()));
    case WIDGET -> widgetIdentityLoader.loadByUid(claims.subject());
    default -> throw new JwtException("Unsupported actor type: " + claims.actorType());
};
```

So there are two switch shapes in this codebase, and the difference is meaningful:

| Switch target | Exhaustive? | Needs `default`? | Example |
| --- | --- | --- | --- |
| `AuthenticatedPrincipal` (sealed interface, pattern-matched) | Yes — compiler-enforced | No | `AuthenticationTokenFactory.create`, `ActorStatusPolicy.isActive` |
| `AuthenticatedActorType` (plain enum, ahead of the sealed permits list) | No | Yes | `JwtAuthenticationFilter.doFilterInternal` |

Implementing a new actor (e.g. `API_KEY`) means two things happen together: a new record joins `AuthenticatedPrincipal`'s `permits` list (which immediately breaks the exhaustive switches until they're given a new `case`), *and* `JwtAuthenticationFilter`'s enum switch gains a real case, shrinking what its `default` branch catches. Until that happens, presenting a JWT with `actorType = API_KEY` fails fast and loud (`JwtException`) rather than being silently mishandled.

---

# The Common Contract

Every implementation guarantees three things, and only three:

| Method | Type | Meaning |
| --- | --- | --- |
| `actorType()` | `AuthenticatedActorType` | Which kind of actor this is — the discriminator `JwtAuthenticationFilter` and `JwtClaims` key off. |
| `subject()` | `UUID` | The actor's own unique identifier — a user's `userUid` or a widget's `widgetUid`. This is what goes into the JWT `sub`-equivalent claim and what `IdentityLoader.loadByUid(...)` looks up on every subsequent request. |
| `tenantUid()` | `UUID` (nullable) | The tenant this actor is scoped to, if any — see below. Present as a field on every implementation; **not** guaranteed non-null on every implementation. |

`tenantUid()` being on the common contract — rather than only on `WidgetPrincipal`, which is the only implementation that actually needs it for scoping — is what lets actor-agnostic code stay actor-agnostic. `DefaultJwtClaimsFactory.accessClaims`/`refreshClaims` call `principal.tenantUid()` directly on the `AuthenticatedPrincipal` interface, with no `instanceof`/switch needed, precisely because the field is guaranteed to exist:

```java
return new JwtClaims(
    principal.actorType(),
    principal.subject(),
    principal.tenantUid(),
    UUID.randomUUID(),
    JwtTokenType.ACCESS,
    now,
    now.plus(jwtProperties.accessTokenLifeTime())
);
```

**Why `UserPrincipal.tenantUid()` is always `null`**: a user can hold membership in more than one organization, so there is no single tenant to bind to the identity itself at authentication time. The field exists purely to satisfy the sealed contract — organization/tenant scoping for a user is resolved per request, per resource, by the Authorization Engine (`ResourceScope`, `MembershipSnapshot` — see **[[Security Architecture]]**, section 8), never carried on the principal.

**Why `WidgetPrincipal.tenantUid()` is always populated**: a widget is never self-provisioned. It is manually created for a business that is already a paying tenant, so a `WidgetPrincipal` without a tenant cannot exist under the current onboarding model — there is exactly one tenant a widget could ever mean.

---

# The Two Implementations

The interface only carries the common contract above — everything actor-specific lives on the concrete type, documented on its own page:

- **[[UserPrincipal]]** — the rich shape: username, roles, permissions, locale, timezone, account status. Produced by `UserPrincipalMapper`, feeds `GrantedAuthorityAdapter` → `UserDetailsAdapter` → `PrincipalUserDetails`.
- **[[WidgetPrincipal]]** — the minimal shape: widget identifier, tenant, status, no roles/permissions. Produced directly by `WidgetIdentityLoader`, bypasses `UserDetailsAdapter`/`PrincipalUserDetails` entirely.

Both implement `Serializable` in addition to `AuthenticatedPrincipal` — the application is fully stateless (`SecurityConfig` sets `SessionCreationPolicy.STATELESS`), so this isn't for HTTP session persistence; it follows the conventional expectation that a Spring Security principal object be serializable, in case the context is ever serialized for another reason (distributed caching, `@Async` context propagation).

---

# Where It's Consumed Generically

The consumers below operate on `AuthenticatedPrincipal` itself — either through the interface's common contract with no branching at all, or through the compiler-enforced exhaustive switch described above. Consumers that narrow to one specific implementation are documented on that implementation's own page (**[[UserPrincipal]]** § Where It's Consumed, **[[WidgetPrincipal]]** § Where It's Consumed) instead of here.

**Identity** (`security/identity/`)
- `PrincipalMapper<T>` — produces an `AuthenticatedPrincipal` from an aggregation (`UserPrincipalMapper` is the only implementation; Widget has no mapper at all).
- `CurrentPrincipalProvider` / `DefaultCurrentPrincipalProvider` — `find()`/`require()` return the interface type generically; only `requireUser()` narrows, and that narrowing is documented on **[[UserPrincipal]]**.

**Token & Session** (`security/token/`)
- `AuthenticationTokenFactory` / `DefaultAuthenticationTokenFactory` — the exhaustive sealed-type switch itself (shown above) belongs here; its two branches are documented on each implementation's page.
- `JwtAuthenticationFilter` — constructs one per request by branching on `JwtClaims.actorType()` (the **enum**, not the sealed type — see "Why Sealed" above), then holds the result generically.
- `JwtClaimsFactory` / `DefaultJwtClaimsFactory` — reads `actorType()`/`subject()`/`tenantUid()` generically to build `JwtClaims`, with no actor-specific branching at all.
- `JwtEngine` — issues tokens from a principal (`issueAccessToken(principal)`/`issueRefreshToken(principal)`), generic over the interface.

**Authorization** (`security/authorization/`)
- `AuthorizationContext.principal()` — every authorization decision is anchored to one, held generically.
- `ActorStatusPolicy` — the exhaustive sealed-type switch shown above, checking `isActive()` regardless of actor without needing a `default`.
- `AuthorizationPermissionEvaluator` — reads the current principal from `CurrentPrincipalProvider` generically to build the `AuthorizationContext` for Spring's `hasPermission(...)` SpEL function.

Nothing in this list holds a reference typed as `UserPrincipal` or `WidgetPrincipal` further upstream than it needs to, which is what keeps the identity and token layers reusable across every current and future actor type.
