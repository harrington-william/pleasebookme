# What It Is

`UserPrincipal` is one of the two implementations of the sealed **[[AuthenticatedPrincipal]]** interface — see that page for the shared contract, why the interface is sealed, and the field every implementation must carry (`actorType`/`subject`/`tenantUid`). This page covers only what's specific to the User actor.

It is the rich shape: username, roles, permissions, locale, timezone, account status — everything a user carries because users participate in RBAC. Every authentication mechanism that ultimately identifies a human (Username/Password, JWT, Google Sign-In) produces the same `UserPrincipal`, never a mechanism-specific variant.

Package: `com.pleasebookme.server.security.identity.principal`. Implements `AuthenticatedPrincipal, Serializable`.

```java
public record UserPrincipal(
    @NotNull
    AuthenticatedActorType actorType,

    UUID subject,
    
    @NotNull
    BigInteger userId,

    // Always null because a user can hold membership in more than 1 organization
    // Kept only to satisfy AuthenticatedPrincipal's sealed contract
    UUID tenantUid,

    @NotBlank(message = "Username is required")
    String username,
    String email,

    String displayName,
    Locale locale,
    @NotBlank(message = "Timezone is required")
    String timezone,

    AccountStatus accountStatus,

    Set<String> roles,
    Set<String> permissions,
    Map<String, Object> attributes

) implements AuthenticatedPrincipal, Serializable {
    public boolean hasRole(String role) { ... }
    public boolean hasPermission(String permission) { ... }
    public boolean isActive() { ... }
}
```

---

# Fields

| Field | Type | Notes |
| --- | --- | --- |
| `actorType` | `AuthenticatedActorType` | Always `USER`. |
| `subject` | `UUID` | The user's `userUid`. |
| `userId` | `BigInteger` | The user's `auth.users.id` — the numeric surrogate PK. See "Why Both `subject` (UUID) and `userId` (BigInteger) Exist" below. |
| `tenantUid` | `UUID` | Always `null` — a user can hold membership in more than one organization, so there is no single tenant to bind at authentication time. Kept only to satisfy `AuthenticatedPrincipal`'s sealed contract; see **[[AuthenticatedPrincipal]]** → "The Common Contract" for the full reasoning. |
| `username` | `String` | Required, non-blank. |
| `email` | `String` | No presence validation on the record itself. |
| `displayName` | `String` | Maps to `UserEntity.name`. |
| `locale` | `Locale` | Shared `global/enums/Locale`, same enum used across `UserEntity`, `ServiceEntity`, etc. |
| `timezone` | `String` | Required, non-blank. |
| `accountStatus` | `AccountStatus` | `auth`-schema enum (`auth.enums.AccountStatus`): `ACTIVE`, `SUSPENDED`, `LOCKED`. Drives `isActive()` and, separately, `PrincipalUserDetails.isAccountNonLocked()` — see below. |
| `roles` | `Set<String>` | Platform-wide role names (e.g. `USER`, `PLATFORM_OWNER`) — **not** organization-scoped. Organization-scoped roles live in `MembershipSnapshot`, resolved separately at authorization time (see **[[Security Architecture]]**, section 8). |
| `permissions` | `Set<String>` | Platform-wide permission slugs (`<RESOURCE>.<ACTION>`), flattened from the user's roles' permissions during `UserPrincipalMapper.map(...)`. |
| `attributes` | `Map<String, Object>` | Currently always `Map.of()` (empty) — populated by `UserPrincipalMapper` but with nothing written into it yet. An extension point, not dead code: `AuthorizationContext` has its own independent `attributes` map for the same purpose at the authorization layer, and this field is the identity-layer analogue. |

---

# Why Both `subject` (UUID) and `userId` (BigInteger) Exist

Same pattern **[[WidgetPrincipal]]** already established for `tenantUid`/`tenantId` — see that page's "Why Both `tenantUid` (UUID) and `tenantId` (BigInteger) Exist" section for the general reasoning. `UserPrincipal` now follows it too:

- `subject` (`UUID`) exists to satisfy `AuthenticatedPrincipal.subject()` — the sealed contract standardizes on `UUID` because that's the external, JWT-safe identifier type (per `CODING_CONVENTIONS.md`'s UID-naming rule). It's what goes into the JWT `sub` claim and what `UserIdentityLoader.loadByUid(...)` looks up on every subsequent request.
- `userId` (`BigInteger`) is the actual surrogate PK (`auth.users.id`), and it's what older or still-unscoped flows that FK into `auth.users` need when they cannot derive ownership server-side yet. Newer ownership-sensitive flows such as schedules, availabilities, and services read it from `CurrentPrincipalProvider.requireUser()` instead of accepting it from request bodies.

**Why this was added, not present from the start**: before this field existed, nothing server-side exposed the caller's numeric `userId` at all — no `/me`-equivalent endpoint returned it, and `AuthenticationAggregation` (which `UserPrincipalMapper` reads) already loads the full `UserEntity`, so `aggregation.user().getUserId()` was sitting right there unused. The consuming client (`client/lib/platform-user.ts`) worked around the gap by fetching the *entire* `GET /api/v1/users` list and scanning for a `userUid` match — an O(n)-in-total-platform-users lookup, run fresh on every call, purely to answer a question the server could answer for free. Adding this field costs nothing new: `AuthenticationAggregation` was already holding the value, so populating it is a field read inside `UserPrincipalMapper.map(...)`, not an extra query.

**This does not by itself fix every workaround.** `GET /api/v1/auth/me` (`AuthController.getCurrentUser()`) returns `UserPrincipal` verbatim, so `userId` is reachable without a server change, but callers that still need profile/organization context for unscoped list filtering continue to resolve it separately. `profileId`/`organizationId` remain deliberately absent from `UserPrincipal` (see below), so a caller needing them still has to resolve them from request-specific organization context rather than identity alone.

---

# Helper Methods

- `hasRole(String role)` — `roles.contains(role)`. Backs `OrganizationIsolationPolicy`'s platform-wide-role bypass check (`PLATFORM_OWNER`/`PLATFORM_MANAGER` skip organization-scoping entirely).
- `hasPermission(String permission)` — `permissions.contains(permission)`. Backs `AuthorizationPolicy.evaluatePermission()`'s default grant check, alongside `MembershipSnapshot.hasPermission(...)`.
- `isActive()` — `accountStatus == AccountStatus.ACTIVE`. Backs `ActorStatusPolicy`'s veto (shared with `WidgetPrincipal.isActive()` through the exhaustive sealed-type switch — see **[[AuthenticatedPrincipal]]**).

---

# What's Deliberately Absent

`UserPrincipal` carries no `organizationId`, `membershipId`, or `profileId` field. This is a real architectural position, not an oversight: identity (who is this user, what can they do platform-wide) and organization context (which org is this request about, what can this user do *in that org*) are resolved at different times, by different components, because a user's organization membership is inherently request-scoped — the same user might be acting on an org they own in one request and an org they're merely staff at in the next. Carrying an organization onto the identity itself would either force a fresh principal per organization switch, or silently go stale. See `MembershipResolver`/`MembershipSnapshot` in **[[Security Architecture]]** section 8 for where that data actually gets resolved instead.

**Why this doesn't contradict `userId` being present.** `userId` is not organization context — it's the same identity fact `subject` already carries, just in the DB's native ID type instead of the external UUID (see "Why Both `subject` (UUID) and `userId` (BigInteger) Exist" above). It answers "which row in `auth.users` is this," a question with exactly one correct answer regardless of which organization a given request concerns. `organizationId`/`profileId` answer "which org, in this request" — a question with a different correct answer per request, which is precisely why they can't be baked onto a principal built once at authentication time.

---

# `@NotNull`/`@NotBlank` on a Record Nobody Validates

`UserPrincipal` carries `jakarta.validation` annotations (`@NotNull` on `actorType`/`userId`, `@NotBlank` on `username`/`timezone`), but nothing in the codebase runs `@Valid` against a `UserPrincipal` — it is never bound from a `@RequestBody`, only ever constructed internally by `UserPrincipalMapper`. The annotations function as inline documentation of the invariants the mapper is expected to uphold, not as an enforced runtime gate. Don't assume a missing field here would be caught by Spring's validation pipeline the way it would on a `Request` DTO.

---

# `Serializable`

`UserPrincipal` implements `Serializable` in addition to `AuthenticatedPrincipal`. The application runs fully stateless (`SecurityConfig` sets `SessionCreationPolicy.STATELESS`), so this isn't for HTTP session persistence — it follows the conventional expectation that a Spring Security principal object (this one ends up wrapped inside `PrincipalUserDetails`, itself stored in `SecurityContextHolder`) be serializable, which matters if the context is ever serialized for any reason (e.g. distributed caching, `@Async` context propagation) even though nothing in the current wiring exercises that path directly.

---

# Where It's Consumed

**Produced by**
- `UserPrincipalMapper.map(AuthenticationAggregation)` — the only place a `UserPrincipal` is ever constructed. Pure transformation, no repository access (see **[[Security Architecture]]**, section 5).

**Identity / SecurityContext integration** (`security/identity/`)
- `GrantedAuthorityAdapter.adapt(UserPrincipal)` — converts `roles` into `ROLE_<name>` authorities and `permissions` slugs into authorities directly (no prefix), deduplicated into one `Collection<GrantedAuthority>`.
- `UserDetailsAdapter.adapt(UserPrincipal, [passwordHash])` — wraps the principal (plus the adapted authorities) into `PrincipalUserDetails`.
- `PrincipalUserDetails` — delegates almost everything back to the wrapped `UserPrincipal`: `getUsername()` → `principal.username()`, `isAccountNonExpired()`/`isEnabled()` → `principal.isActive()`. `isAccountNonLocked()` is the one exception — it checks `accountStatus() != AccountStatus.LOCKED` directly rather than going through `isActive()`, which is why `SUSPENDED` and `LOCKED` are distinguishable at the Spring Security boundary even though both make `isActive()` false.
- `CurrentPrincipalProvider.requireUser()` — narrows the generic `AuthenticatedPrincipal` back down to `UserPrincipal`, throwing `ForbiddenActorException` (403) for any other actor type.

**Token & Session** (`security/token/`)
- `DefaultAuthenticationTokenFactory.createUserAuthentication` — the `UserPrincipal` branch of the sealed-type switch, producing a `UsernamePasswordAuthenticationToken` via the `UserDetailsAdapter`/`PrincipalUserDetails` pair above.

**Authorization** (`security/authorization/`)
- `AuthorizationPolicy.evaluatePermission()` (default method) — narrows to `UserPrincipal` via `instanceof`, checking `hasPermission(slug)` or the resolved `MembershipSnapshot.hasPermission(slug)`. Abstains for any other actor type.
- `OrganizationIsolationPolicy` — narrows to `UserPrincipal` via `instanceof`. Bypasses entirely for `PLATFORM_OWNER`/`PLATFORM_MANAGER` roles; otherwise denies if the resource has no resolvable organization scope, or if the user holds no accepted membership in that organization.

**Reachable externally**
- `GET /api/v1/auth/me` (`AuthController.getCurrentUser()`) returns `currentPrincipalProvider.requireUser()` — i.e. `UserPrincipal` serialized verbatim, `userId` included, with no separate DTO in between.

**Current Consumers**

`ScheduleServiceImpl`, `AvailabilityServiceImpl`, and `BusinessServiceImpl` (`core/service/`) now read `principal.userId()` through `CurrentPrincipalProvider.requireUser()` for server-side ownership derivation instead of accepting the caller's `userId` in those request bodies. `BusinessServiceImpl` still derives `profileId`/`organizationId` from `ProfileRepository.findByUserUserId(userId)` because those fields remain deliberately absent from `UserPrincipal`; that query expects at most one profile and will throw if a multi-organization user has more than one profile until request-scoped organization context is introduced.
