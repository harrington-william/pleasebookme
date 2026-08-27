

```bash
Spring Filter Chain
        │
        ▼
IdentityLoader
        │
        ▼
AuthenticationAggregation
        │
        ▼
UserPrincipalMapper
        │
        ▼
AuthenticatedPrincipal
        │
        ├──────────────► Authorization Engine
        │
        ▼
GrantedAuthorityAdapter
        │
        ▼
UserDetailsAdapter
        │
        ▼
PrincipalUserDetails
        │
        ▼
UsernamePasswordAuthenticationToken
        │
        ▼
SecurityContextHolder
```

# 1. Overview

The security architecture defines how untrusted actors can be authenticated and converted into a canonical business domain identity before falling into authorization engine, while maintaining a strict separation between the business and framework boundaries. You can inspect the flow by seeing **[[The Security Big Picture]]**.

The primary architectural objective is to ensure that the authentication domain owns all business concepts related to authorization, while Spring Security remains an infrastructure concern responsible only for integrating with the application framework.

This design prevents framework-specific abstractions from leaking into business logic and allows the platform to evolve independently of Spring Security.

---

# 2. Framework Boundary

Spring Security requires authenticated identities to implement framework-specific contracts such as `UserDetails` and `Authentication`.

Although these abstractions are useful for integrating with the framework, they should not become part of the domain model because they tightly couple business logic to a specific security implementation.

Instead, the platform introduces a canonical business identity called **[[AuthenticatedPrincipal]]**.

Every authentication mechanism—including Username/Password, JWT, OAuth, API Keys, Widgets, and future Service Accounts—ultimately produces an identity model, which implement [[AuthenticatedPrincipal]].

Business authorization decisions are always performed using [[AuthenticatedPrincipal]] rather than Spring Security interfaces.

---

# 3. Architectural Principles

The security architecture is not a single pipeline — it is four cooperating subsystems, each owning one question, each isolated from the others' internals:

| Subsystem             | Question it answers                                                | Location                  |
| --------------------- | ------------------------------------------------------------------ | ------------------------- |
| Identity              | Who is the current actor?                                          | `security/identity/`      |
| Token & Session       | How does that identity survive across requests?                    | `security/token/`         |
| Authorization         | Is this actor allowed to do this, to this resource, in this scope? | `security/authorization/` |
| Credential Encryption | How are long-lived third-party credentials kept safe at rest?      | `security/crypto/`        |

Every subsystem is described in its own section below. The principles that hold all four together:

## Framework Isolation

Business code must never depend directly on Spring Security classes.

Only the infrastructure layer is aware of:

- UserDetails
- Authentication
- GrantedAuthority
- SecurityContextHolder
- PermissionEvaluator

The business layer only understands:

- AuthenticatedPrincipal
- AuthorizationContext / AuthorizationDecision
- JwtClaims

---

## Canonical Identity Model

The platform defines exactly one authenticated identity representation.

```java
AuthenticatedPrincipal
```

`AuthenticatedPrincipal` is a **sealed interface** — `permits UserPrincipal, WidgetPrincipal` — implemented by every authentication mechanism the platform supports:

- Username & Password
- JWT (the mechanism that reconstructs a principal on every subsequent request)
- Google OAuth (Sign-In)
- Widget Token
- Future: API Keys, Internal Service Accounts

Because the interface is sealed, any code that switches over `AuthenticatedPrincipal` is checked exhaustively by the compiler — adding a new actor type is a compile error everywhere that switch isn't updated, not a silent runtime gap. Every implementation guarantees `actorType()`, `subject()`, and `tenantUid()`; each concrete type carries only the identity data that actor actually has. `UserPrincipal` is the rich shape (username, roles, permissions, locale, timezone, account status) because users participate in RBAC. `WidgetPrincipal` is deliberately minimal (widget identifier, tenant, status) because a widget is a single-purpose actor bound to one tenant with a fixed capability set, not an RBAC participant.

**Identity, deliberately, does not carry organization or membership.** `UserPrincipal` has no organization/membership/profile fields, and its `tenantUid` is always `null` — a user can hold membership in more than one organization, so there is no single tenant to bind at authentication time. Which organization a request is acting within, and what that user's role is *inside* that organization, is resolved fresh per request by the Authorization Engine (section 8), not baked into the token at login. This is a deliberate trade-off: identity stays cheap, stable, and unambiguous; organization-scoped facts are resolved at the point they're actually needed, against current data, for exactly the organization the request concerns.

---

## Actor-Agnostic Token & Session Model

A single JWT claim shape (`JwtClaims`: `actorType`, `subject`, `tenant`, `tokenId`, `tokenType`, `issuedAt`, `expiresAt`) and a single filter (`JwtAuthenticationFilter`) serve every actor type. The filter verifies the token once, then branches on `claims.actorType()` to pick the matching identity loader. Adding a new actor type never means adding a new filter or a new token format — see section 7.

---

## Policy-Based, Data-Driven Authorization

Authorization is not a scatter of `if (user.hasRole(...))` checks inside controllers and services. It is a registry of independent `AuthorizationPolicy` implementations, each free to abstain, permit, or deny a given resource-type/action pair, composed by one fixed decision algebra. New authorization rules are added by registering a new Spring bean, not by editing existing business logic — see section 8.

---

## Separation of Responsibilities

| Component                              | Subsystem             | Responsibility                                          |
| -------------------------------------- | --------------------- | ------------------------------------------------------- |
| IdentityLoader                         | Identity              | Load authentication data from persistence               |
| AuthenticationAggregation              | Identity              | Authentication read model (User only)                   |
| PrincipalMapper                        | Identity              | Convert aggregation into business identity              |
| AuthenticatedPrincipal                 | Identity              | Canonical authenticated identity                        |
| GrantedAuthorityAdapter                | Identity              | Convert business roles into Spring authorities          |
| UserDetailsAdapter                     | Identity              | Adapt business identity into Spring UserDetails         |
| PrincipalUserDetails                   | Identity              | Spring Security adapter                                 |
| CurrentPrincipalProvider               | Identity              | Read the principal back out of the SecurityContext      |
| JwtEngine / JwtGenerator / JwtVerifier | Token                 | Issue and verify actor-agnostic JWTs                    |
| JwtAuthenticationFilter                | Token                 | Per-request verification + principal reconstruction     |
| TokenRefresher / RefreshTokenVerifier  | Token                 | Rotate access/refresh token pairs                       |
| AuthorizationPolicy (+ registry)       | Authorization         | Express one rule for one resource type (or all of them) |
| ScopeResolver (+ registry)             | Authorization         | Derive a resource's organization/tenant/owner scope     |
| MembershipResolver                     | Authorization         | Resolve a user's per-organization roles/permissions     |
| AuthorizationService                   | Authorization         | Run the policy set, produce one decision                |
| AuthorizationPermissionEvaluator       | Authorization         | Bridge the engine into Spring's `@PreAuthorize`         |
| TokenCipher / AesGcmTokenCipher        | Credential Encryption | Encrypt/decrypt long-lived third-party credentials      |

Each component performs exactly one responsibility, and no component reaches across subsystem boundaries except through the types listed above.

---

# 4. Authentication Pipeline

Not every actor needs the same pipeline depth. The full pipeline exists because User's identity requires joining multiple tables (user, roles, permissions); Widget's identity is a single-entity lookup, so it skips the aggregation/mapping stages entirely.

**User** — the full pipeline:

```
Database
        │
        ▼
UserIdentityLoader
        │
        ▼
AuthenticationAggregation
        │
        ▼
UserPrincipalMapper
        │
        ▼
AuthenticatedPrincipal (UserPrincipal)
        │
        ├──────────────► Authorization Engine
        │
        ▼
GrantedAuthorityAdapter
        │
        ▼
UserDetailsAdapter
        │
        ▼
PrincipalUserDetails
        │
        ▼
UsernamePasswordAuthenticationToken
        │
        ▼
SecurityContextHolder
```

**Widget** — the shortened pipeline:

```
Database
        │
        ▼
WidgetIdentityLoader
        │
        ▼
AuthenticatedPrincipal (WidgetPrincipal)
        │
        ▼
PreAuthenticatedAuthenticationToken
        │
        ▼
SecurityContextHolder
```

`WidgetIdentityLoader` queries `WidgetEntity` directly and constructs `WidgetPrincipal` in one step — there is no aggregation and no mapper, because there is no multi-repository join to justify the extra stages. This is the template for any future low-complexity actor (e.g. API Keys, if their identity resolves from one row): reuse the shortened shape rather than introducing an aggregation/mapper pair with nothing to aggregate or map.

`WidgetIdentityLoader` is also where a pre-authentication gate lives: before it will construct a principal at all, it checks that the request's `Origin` header matches one of the widget's registered origins (`widget_origins`), and that the widget is `ACTIVE` and unexpired. A failed check throws before any `WidgetPrincipal` exists. This gate is not authorization (it doesn't ask what the actor may do) and it isn't carried on the principal afterward (origin isn't an ongoing identity property) — it belongs in the loader because the loader already owns "decide whether this actor can be resolved from persistence at all."

Notice, in both shapes, that the business identity is constructed **before** interacting with Spring Security. The framework only ever receives an adapter — or, for Widget, the bare principal — wrapped around a business object that already exists.

---

# 5. Identity Component Responsibilities

## IdentityLoader

Responsible for assembling everything required to authenticate an actor. `UserIdentityLoader` loads the user plus roles and their transitive permissions (`AuthenticationAggregation`) — nothing about organization, membership, or tenant. `WidgetIdentityLoader` loads a single `WidgetEntity` plus performs the origin/status/expiry gate described above. `IdentityLoader` implementations are the only components that query repositories during authentication; they make no authorization decisions.

## AuthenticationAggregation

A read model used exclusively during authentication, existing only for User. It is **not** a Domain-Driven Design aggregate — it is an authentication-specific projection (`UserEntity`, `Set<RoleEntity>`, `Set<PermissionEntity>`) that keeps the multi-table join in one place, out of the mapper.

## PrincipalMapper

Transforms `AuthenticationAggregation` into `AuthenticatedPrincipal`. `PrincipalMapper<T>` is generic; `UserPrincipalMapper` is its only implementation. It performs pure object transformation with no repository access, which is what makes it reusable — Sign-In, Register, and Refresh all rejoin the pipeline at this exact stage. Widget has no mapper, for the same reason it has no aggregation.

## AuthenticatedPrincipal

The canonical representation of an authenticated actor, belonging entirely to the security domain, with no framework-specific interfaces anywhere in it. See "Canonical Identity Model" above for the sealed-interface contract and why organization/tenant data is deliberately absent from it. This object — not `UserDetails`, not `Authentication` — is what the Authorization Engine consumes (section 8).

## GrantedAuthorityAdapter

Converts business `Role`/`Permission` names into Spring's `GrantedAuthority`, with no business logic of its own — a pure format conversion for the one place Spring Security needs authorities (method-security expressions that check `hasRole`/`hasAuthority`, as distinct from the resource-aware `hasPermission(...)` path the Authorization Engine drives — see section 8).

## UserDetailsAdapter / PrincipalUserDetails

`UserDetailsAdapter` converts `AuthenticatedPrincipal` into `PrincipalUserDetails`, the boundary object between the business model and Spring Security. `PrincipalUserDetails` implements `UserDetails`, delegates everything to the wrapped `AuthenticatedPrincipal`, and contains no business rules of its own. Both exist only for `UserPrincipal` — Widget bypasses this pair entirely (section 6).

## CurrentPrincipalProvider

The single sanctioned way for code outside the security package to ask "who is calling?" Controllers must use it — never `@AuthenticationPrincipal UserPrincipal`, which silently binds to `null` because `AuthenticationTokenFactory` stores `PrincipalUserDetails` (a wrapper), not `UserPrincipal` itself.

- `find()` — `Optional<AuthenticatedPrincipal>`, empty when unauthenticated or anonymous.
- `require()` — the principal, or throws `UnauthenticatedException` (401).
- `requireUser()` — narrows to `UserPrincipal`, throwing `ForbiddenActorException` (403) for any other actor type.

The asymmetry is deliberate: no principal at all is retryable (401 — supply credentials and try again); a present-but-wrong actor type is not (403 — a widget token will never be sufficient for a user-scoped operation, so retrying is pointless).

---

# 6. SecurityContext Integration

Spring Security requires an `Authentication` object in `SecurityContextHolder`, and the concrete shape of that object differs by actor type — this is the one place the two identity pipelines visibly diverge downstream of the principal itself.

`AuthenticationTokenFactory` is the single component that decides the shape, switching on the sealed `AuthenticatedPrincipal` type (exhaustively — a new actor type without a new branch here is a compile error):

- `UserPrincipal` → `UserDetailsAdapter` → `PrincipalUserDetails` → `UsernamePasswordAuthenticationToken`. This shape exists because User authentication has password/account-lock concepts for `UserDetails` to represent.
- `WidgetPrincipal` → stored directly as the principal on a `PreAuthenticatedAuthenticationToken`, Spring's construct for an actor already authenticated by a mechanism outside the standard username/password filter chain. A widget authenticated by public/secret key plus origin validation has none of `UserDetails`' password semantics, so wrapping it in one would be modeling a lie.

The SecurityContext therefore stores framework-specific objects whose shape depends on the actor — but business code never touches either shape directly. `CurrentPrincipalProvider` (section 5) is what normalizes both back to `AuthenticatedPrincipal`, which is why business code remains completely unaware of which path a given request took.

`JwtAuthenticationFilter` is the entry point that drives this on every JWT-bearing request: verify the token, branch on `claims.actorType()` to the matching `IdentityLoader`, hand the resulting principal to `AuthenticationTokenFactory`, store the result. See section 7 for the token shape that makes one filter sufficient for every actor.

---

# 7. Token & Session Architecture

The token layer is what lets a principal, constructed once at login, be reconstructed identically on every subsequent request without re-running the full authentication pipeline each time.

```
Login / Register / Refresh
        │
        ▼
JwtEngine.issueAccessToken(principal) / issueRefreshToken(principal)
        │
        ▼
JwtClaimsFactory  ──►  JwtClaims { actorType, subject, tenant, tokenId, tokenType, issuedAt, expiresAt }
        │
        ▼
JwtGenerator  ──►  signed JWT string
```

```
Every subsequent request
        │
        ▼
JwtAuthenticationFilter  (reads Authorization: Bearer <token>)
        │
        ▼
JwtEngine.verify(token)  ──►  JwtVerifier + JwtKeyProvider  ──►  JwtClaims
        │
        ▼
switch (claims.actorType())
        │
        ├── USER   → UserIdentityLoader.loadByUid(subject) → UserPrincipalMapper
        └── WIDGET → WidgetIdentityLoader.loadByUid(subject)
        │
        ▼
AuthenticationTokenFactory.create(principal)
        │
        ▼
SecurityContextHolder
```

`JwtClaims` is intentionally actor-agnostic — the same shape, the same `JwtEngine`, and the same `JwtAuthenticationFilter` serve both User and Widget access tokens. Adding a future actor type costs a new branch in the filter's `switch` and a new `IdentityLoader`, never a new token format.

**Refresh is a separate concern from access.** `TokenRefresher` verifies a presented refresh token via `RefreshTokenVerifier` (checked against the persisted `auth.refresh_tokens` row, not just the JWT signature — a token can be structurally valid and still revoked), reloads the principal through the same `UserIdentityLoader` → `UserPrincipalMapper` path login uses, revokes the old refresh token, and issues a new access/refresh pair. Reusing the same loader/mapper path is what keeps refreshed sessions consistent with freshly logged-in ones — there is exactly one way a `UserPrincipal` gets built from a `UserEntity`, not two that could drift apart.

---

# 8. Authorization Engine

Authentication answers "who is this actor?" Authorization answers "may this actor perform this action, on this resource, in this scope?" The engine is built to answer that question the same way regardless of which controller asks it, without any controller or service encoding the rule itself.

## Inputs

`AuthorizationContext` is the complete question, assembled by the caller:

```
AuthorizationContext {
    principal        AuthenticatedPrincipal   // who
    resourceType      String                  // e.g. "BOOKING"
    action            String                  // e.g. "CREATE"
    resource          Object                   // the actual entity, if one exists yet
    scope             ResourceScope           // organizationId / tenantId / ownerUserId
    membership        MembershipSnapshot      // this user's roles+permissions in THIS org
    attributes        Map<String, Object>
}
```

`permissionSlug()` derives `"<resourceType>.<action>"` (e.g. `BOOKING.CREATE`) — the same slug vocabulary `auth.permissions` already seeds.

`ResourceScope` (`organizationId`, `tenantId`, `ownerUserId`) is how "which organization/tenant does this resource belong to" travels through the engine without coupling policies to specific entity types. `ScopeResolver<T>` maps one entity type to a `ResourceScope` (e.g. `BookingScopeResolver` walks `Booking → Service → Organization`, then looks up that organization's tenant); `ScopeResolverRegistry` dispatches to the resolver matching the resource's runtime type, or returns `ResourceScope.unscoped()` when none is registered.

`MembershipSnapshot` (`membershipId`, `organizationId`, `roles`, `permissions`) is the per-organization counterpart to `UserPrincipal`'s platform-wide roles/permissions — resolved fresh per request by `MembershipResolver` from `organization.memberships` + `organization.membership_roles`, exactly because (per section 3) identity itself doesn't carry it. A user can be an OWNER in one organization and hold no membership in another; `AuthorizationContext` only ever carries the snapshot for the one organization the current request concerns.

## Decision algebra

`AuthorizationPolicy` is the unit of rule:

```
resourceType()          "*" (wildcard, applies to every resource type) or a specific type
supports(action)        does this policy have an opinion on this action at all
order()                 evaluation order within the resolved set
evaluate(context)       → AuthorizationDecision { PERMIT | DENY | ABSTAIN }
```

`AuthorizationPolicyRegistry` indexes every registered policy bean by `resourceType()` at startup, and separately holds every wildcard (`*`) policy. `resolve(resourceType, action)` returns wildcard policies **and** resource-specific policies for that type, filtered to those where `supports(action)` is true, sorted by `order()`.

`AuthorizationService.authorize(context)` runs that resolved list through one fixed algebra:

1. Any `DENY` short-circuits — the whole authorization call denies immediately, regardless of what runs after it.
2. Otherwise, the first `PERMIT` seen is remembered.
3. If nothing ever denied and nothing ever permitted (every policy `ABSTAIN`ed, or no policy matched at all), the result is a default **deny** — `NO_POLICY`.

This is deny-overrides with default-deny — permission must be affirmatively granted by at least one policy, and any single objection wins regardless of order. `require(context)` is the same call, throwing `AuthorizationDeniedException` when the result isn't granted. A policy that throws during evaluation is wrapped in `PolicyEvaluationException` rather than propagating raw — one misbehaving policy fails loud and identifiable, not as a silent authorization bypass.

## The built-in wildcard policies

Four policies ship registered against `*`, meaning every resource type and action passes through them:

| Policy | Order | Rule |
| --- | --- | --- |
| `ActorStatusPolicy` | -20 | Deny if the actor (User or Widget) isn't in an active status. Otherwise abstain — it never permits, only vetoes. |
| `WidgetCapabilityPolicy` | -10 | For a `WidgetPrincipal`: permit only if the permission slug is in a fixed allow-list (`AVAILABILITY.READ`, `SERVICE.READ`, `SELECTEDSLOT.CREATE/DELETE`, `BOOKING.CREATE`, `ATTENDEE.CREATE`); deny everything else. Abstains for any other actor type. |
| `OrganizationIsolationPolicy` | 0 | For a `UserPrincipal` without a platform-wide role (`PLATFORM_OWNER`/`PLATFORM_MANAGER`): deny if the resource has no resolvable organization scope, or if the user holds no accepted membership in that organization. Otherwise abstain — this policy only ever narrows, the actual permission grant comes from `AuthorizationPolicy.evaluatePermission()`. |
| `WidgetTenantIsolationPolicy` | 0 | For a `WidgetPrincipal`: deny if the resource's tenant scope doesn't match the widget's own tenant. Otherwise abstain. |

Evaluation order is deliberate: actor-status and widget-capability run first (`-20`, `-10`) as fast, cheap vetoes before any scope/membership reasoning runs at order `0`. A resource-specific policy (registered against a concrete `resourceType()` rather than `*`) is where the actual permission grant for that resource type lives, typically via `AuthorizationPolicy.evaluatePermission()` — the default method every policy inherits, which permits when either the platform-wide `UserPrincipal.hasPermission(slug)` or the resolved `MembershipSnapshot.hasPermission(slug)` holds the matching `<RESOURCE>.<ACTION>` slug.

## Spring integration

`AuthorizationPermissionEvaluator` implements Spring Security's `PermissionEvaluator`, registered into method security via `AuthorizationExpressionHandlerConfig`'s `MethodSecurityExpressionHandler` bean. This is what makes `@PreAuthorize("hasPermission(#id, 'BOOKING', 'CREATE')")` on a controller method route into the engine above: the evaluator reads the current principal from `CurrentPrincipalProvider`, splits the permission string into resource type and action, builds an `AuthorizationContext`, and calls `AuthorizationService.authorize(...)`.

**Current wiring boundary, worth knowing before relying on it**: the `PermissionEvaluator` entry point builds its `AuthorizationContext` with `ResourceScope.unscoped()` and no `MembershipSnapshot` — it does not yet call `ScopeResolverRegistry` or `MembershipResolver` itself. A scoped policy (`OrganizationIsolationPolicy`, `WidgetTenantIsolationPolicy`) reached through `@PreAuthorize("hasPermission(...))")` will therefore deny on `OUT_OF_SCOPE` unless the caller happens to hold a platform-wide role. Full scope/membership resolution is proven working (`BookingScopeResolver`, `DefaultMembershipResolver`, both unit-tested), but nothing in the controller layer calls `@PreAuthorize` yet — this is infrastructure built ahead of its consumer, the same pattern the codebase already follows for the `auth.permissions` `SESSION.*` seed rows and the `api_keys` table. Wiring the evaluator to resolve scope/membership per-resource (or having callers build the full `AuthorizationContext` directly and call `AuthorizationService.require(...)` themselves) is the remaining step before `@PreAuthorize` is safe to sprinkle across controllers wholesale.

---

# 9. Credential Encryption at Rest

Authorization decides who may act; encryption protects what a successful authorization then hands over. `integration.oauth_connections` stores Google OAuth access/refresh tokens — a refresh token in particular is an effectively-permanent credential to a user's Calendar/Sheets/Drive, valuable in a database dump or backup long after any breach, and not something to store in plaintext the way a rotating access token might be tolerated.

`TokenCipher` (`AesGcmTokenCipher` is the only implementation) does AES-256-GCM with a fresh 12-byte IV per encryption prepended to the ciphertext — GCM fails catastrophically on IV reuse, so this is structural, not a convention to remember. The 128-bit authentication tag means tampering is detected on decrypt, not silently accepted as garbage plaintext.

Keys are a version-to-key map with one designated current version, validated at startup — a key that isn't valid Base64 or isn't exactly 32 bytes fails the boot, not the first request. `decrypt(ciphertext, keyVersion)` takes the version as an explicit parameter rather than reading it from the payload, which is what makes `oauth_connections.token_key_version` do real work: rotating keys is a configuration change (add key 2, make it current), new writes use it, and every existing row keeps decrypting correctly against the version already recorded against it — no re-encryption sweep, no downtime.

`TokenEncryptionException` is deliberately **not** wired into `GlobalExceptionHandler`. A decryption failure is a server fault, and surfacing cipher internals to a client is an information leak — it falls through to a generic 500 by design.

---

# 10. Dependency Direction

Dependencies flow only in one direction, and this holds independently across all three request-facing subsystems.

**Identity → Framework:**

```
AuthenticatedPrincipal
        │
        ▼
PrincipalUserDetails / (bare principal for Widget)
        │
        ▼
Authentication
        │
        ▼
SecurityContextHolder
```

**Authorization → Framework:**

```
AuthorizationContext / AuthorizationDecision
        │
        ▼
AuthorizationPermissionEvaluator
        │
        ▼
PermissionEvaluator (Spring)
```

The reverse dependency is intentionally prohibited in both directions. `AuthenticatedPrincipal` has no knowledge of `UserDetails`, `Authentication`, `GrantedAuthority`, or `SecurityContextHolder`. `AuthorizationContext`, `AuthorizationPolicy`, and `AuthorizationDecision` have no knowledge of `PermissionEvaluator` or any other Spring Security type — the entire policy engine in section 8 can be, and is, unit tested with nothing but plain Java objects. This is what keeps the security domain portable, testable, and independent of the specific framework wiring it happens to sit behind today.

---

# 11. Architectural Benefits

## Framework Independence

Business logic remains isolated from Spring Security. Changing authentication frameworks requires modifications only within the adapter layer (`identity/adapter/`, `authorization/adapter/`).

## Single Responsibility

Each component performs one well-defined responsibility. Loading, mapping, adaptation, token issuance, and authorization remain independent concerns — none of them reach into another's internals, only through the shared types (`AuthenticatedPrincipal`, `AuthorizationContext`, `JwtClaims`).

## Extensibility

A new authentication mechanism needs a new `IdentityLoader` (and only an `AuthenticationAggregation`/`PrincipalMapper` pair if its identity genuinely requires a multi-table join), a new branch in `AuthenticationTokenFactory`'s switch, and a new permitted type on `AuthenticatedPrincipal` — the compiler enforces the last two aren't skipped. A new authorization rule needs a new `AuthorizationPolicy` bean, registered automatically by Spring, with zero changes to existing policies or to any controller. A new resource type needing scope-aware authorization needs a new `ScopeResolver<T>` bean, nothing more.

## Testability

Both the identity pipeline and the authorization engine can be unit tested using plain domain objects (`AuthenticatedPrincipal`, `AuthorizationContext`), without constructing a `SecurityContextHolder` or mocking any Spring Security type.

## Maintainability

Framework code remains localized within infrastructure (`identity/adapter/`, `authorization/adapter/`, `token/filter/`). Business code operates exclusively on domain concepts, reducing coupling and simplifying long-term evolution.

---

# 12. Future Evolution

This architecture is the foundation for every authentication mechanism the platform has added since, and every one has slotted in without changing the shape described above:

- **JWT** did not replace `AuthenticatedPrincipal` — it is the mechanism for reconstructing the same canonical identity on every request (section 7), verified once per request and handed off to the same `AuthenticationTokenFactory`/`SecurityContextHolder` integration regardless of actor type.
- **Widget authentication** proved the shortened pipeline (section 4) and the `PreAuthenticatedAuthenticationToken` branch (section 6) — introduced no new stage in the full pipeline, only a parallel shorter one.
- **Google Sign-In** introduced no new actor type at all — it produces the same `UserPrincipal` Username/Password produces and rejoins the standard pipeline at `UserIdentityLoader`, which is why it required no change to `AuthenticatedPrincipal`'s permitted types, no new `IdentityLoader`, and no new branch in `AuthenticationTokenFactory`. A new *credential type* is not the same thing as a new *actor type*, and only the latter costs anything architecturally. (Google's delegated-authorization flow — connecting Calendar/Sheets/Drive — is a separate concern from authentication entirely; it produces no principal and is covered by the encryption-at-rest subsystem in section 9, not by this pipeline.)

Remaining known work follows the same shape rather than requiring new architecture:

- **API Keys / Service Accounts**: a new `IdentityLoader` (shortened-pipeline shaped, per the Widget precedent, unless the identity genuinely needs a join), a new permitted type on `AuthenticatedPrincipal`, a new branch in `AuthenticationTokenFactory` and in `JwtAuthenticationFilter`'s `switch`.
- **Widget capability model**: `WidgetPrincipal` still carries no scopes/authorities of its own — `WidgetCapabilityPolicy`'s fixed allow-list (section 8) is the capability model today. A per-widget, non-RBAC scope set (reusing the `auth.permissions` slug vocabulary) is the anticipated replacement.
- **Wiring `@PreAuthorize` into controllers**: the authorization engine is complete and tested end to end except for the scope/membership resolution step inside `AuthorizationPermissionEvaluator` itself (section 8's wiring boundary) — closing that gap is what turns the engine from "available" into "enforced" at the controller layer.

Regardless of which of these lands next, the invariant holds: authentication produces `AuthenticatedPrincipal`, authorization consumes `AuthorizationContext` built around one, and neither ever needs to know which mechanism was actually used.
