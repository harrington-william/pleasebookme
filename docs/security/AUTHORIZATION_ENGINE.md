# Authorization Engine

## 1. Scope

`SECURITY.md` covers authentication — establishing `AuthenticatedPrincipal`, the canonical
identity every actor type (`UserPrincipal`, `WidgetPrincipal`) resolves to. This document
covers what happens *after* that: given a known actor, may they perform a specific action
on a specific resource.

Authentication answers **who is this actor?** Authorization answers **is this actor allowed
to perform this action, on this resource, under these conditions?** The engine described
here is the second question only. It consumes `AuthenticatedPrincipal`; it has no knowledge
of JWTs, Spring Security, or how the principal was constructed.

Package root: `security/authorization/`. Domain-specific policies live next to the domain
they govern (e.g. `core/booking/authorization/`), not inside the engine package itself.

## 2. Two security boundaries

The engine enforces two independent boundaries, evaluated in this order:

| Boundary | Question | Answered by |
|---|---|---|
| Application boundary | Is this actor's account/widget in a state that may act at all? | `ActorStatusPolicy` |
| Organization boundary | Does *this specific membership* permit *this actor* to perform *this action* on *this resource*, under this organization? | membership-scoped RBAC policies |

The application boundary is actor-global — an account can be suspended regardless of which
organization it's touching. The organization boundary is per-request and per-resource — the
same user can be fully permitted in one organization and have no standing at all in another.
Conflating the two was the original design mistake this engine was rebuilt to avoid: RBAC
checks that only asked "does this role have this permission" without asking "does this
specific membership, in this specific organization, actually grant it."

## 3. Core components

| Component | Kind | Role |
|---|---|---|
| `AuthorizationContext` | record | Everything one authorization decision needs: principal, resource type, action, the loaded resource (nullable), its resolved `ResourceScope`, the caller's resolved `MembershipSnapshot` (nullable), free-form attributes |
| `ResourceScope` | record | Where a resource lives: `organizationId`, `tenantId`, `ownerUserId` — all independently nullable |
| `MembershipSnapshot` | record | A resolved, *accepted* membership for one specific organization: `membershipId`, `organizationId`, org-scoped `roles`, org-scoped `permissions` |
| `AuthorizationDecision` | record | `Effect` (`PERMIT`/`DENY`/`ABSTAIN`), a client-safe `code`, a server-only `reason`, and `policyName` for audit attribution |
| `AuthorizationPolicy` | interface | One unit of policy logic: `resourceType()`, `supports(action)`, `order()`, `evaluate(context)`, plus a shared `evaluatePermission(context)` default method |
| `AuthorizationPolicyRegistry` / `DefaultAuthorizationPolicyRegistry` | interface / impl | Indexes every `AuthorizationPolicy` bean by resource type at startup; answers "which policies apply to this resource type + action" |
| `AuthorizationService` / `DefaultAuthorizationService` | interface / impl | Runs the resolved policy list through the combination algorithm and produces one final decision |
| `ScopeResolver<T>` / `ScopeResolverRegistry` | interface / impl | Per-entity-type: given a loaded resource, compute its `ResourceScope` |
| `MembershipResolver` / `DefaultMembershipResolver` | interface / impl | Given a user and a target organization, resolve their accepted membership (if any) into a `MembershipSnapshot` |
| `AuthorizationPermissionEvaluator` | Spring bridge | Implements Spring Security's `PermissionEvaluator`, wired via `AuthorizationExpressionHandlerConfig` so `@PreAuthorize("hasPermission(...)")` can route through this engine |

## 4. The evaluation pipeline, end to end

```
caller (controller/service)
   │  resolves the target org's ResourceScope   (ScopeResolverRegistry, once the resource is loaded)
   │  resolves the caller's MembershipSnapshot  (MembershipResolver, keyed on that same organization)
   ▼
AuthorizationContext (immutable, fully assembled before any policy runs)
   │
   ▼
AuthorizationService.authorize(context) / .require(context)
   │
   ▼
AuthorizationPolicyRegistry.resolve(resourceType, action)
   │  merges wildcard policies + resource-specific policies,
   │  filters by supports(action), sorts by order()
   ▼
ordered List<AuthorizationPolicy>
   │
   ▼
combination algorithm (§5) → one AuthorizationDecision
   │
   ▼
authorize() returns it  /  require() throws AuthorizationDeniedException if not granted
```

The context is built once, by the caller, before evaluation starts. Policies never mutate
it and never communicate with each other directly — the only channel between policies is
each one's own `AuthorizationDecision`, combined by the service. This is why scope and
membership are resolved by the caller *ahead of* calling into the engine, rather than by a
policy mid-pipeline: nothing downstream of the first policy could hand data to a later one.

## 5. The decision-combination algorithm

`DefaultAuthorizationService.authorize(context)`:

1. Ask the registry for every policy that applies to this `resourceType` + `action`, already
   ordered.
2. Walk the list. Any `DENY` returns **immediately** — no further policy in the list is
   evaluated. This is a true short-circuit, not just "the final answer is deny."
3. A `PERMIT` is remembered, but does **not** stop the loop — scanning continues in case a
   later, lower-priority-in-list-but-still-relevant policy still denies.
4. `ABSTAIN` does nothing; the loop continues.
5. If the loop finishes with no `DENY` ever seen: return the remembered `PERMIT`, or if
   none was ever set, return a synthetic `DENY` with code `NO_POLICY`.

Consequences worth being explicit about:

- **`authorize()` can only ever return `PERMIT` or `DENY`** — never `ABSTAIN`. A pipeline
  where every policy abstains still produces a `DENY` (`NO_POLICY`). This is what makes the
  engine deny-by-default: a resource type with zero registered policies is unauthorized, not
  open.
- **A later `DENY` always overrides an earlier `PERMIT`.** This is intentional and it is
  load-bearing for actor-specific wildcard policies — see §6, `WidgetCapabilityPolicy`.
- **`order()` only matters for two things**: which policy's `DENY` reason is the one
  actually returned when multiple policies would deny, and how early a cheap check runs
  relative to an expensive one. It does **not** determine whether the final result is grant
  or deny when a `PERMIT` and a `DENY` are both present — the `DENY` always wins regardless
  of which one has the lower `order()`.
- **Any unchecked exception thrown by a policy aborts the entire check** — wrapped as
  `PolicyEvaluationException`, not swallowed or treated as an implicit abstain. It is not
  registered in `GlobalExceptionHandler`, so it surfaces as a generic 500: a broken policy is
  a server fault, and its internals should not reach a client.
- **`require(context)`** is `authorize(context)` plus a throw on non-grant
  (`AuthorizationDeniedException`, carrying the full decision). `GlobalExceptionHandler`
  maps its `code` to HTTP status: `"OUT_OF_SCOPE"` → 404 (a cross-organization resource
  should look like it doesn't exist, not confirm its existence with a 403), everything
  else → 403.

All of the above is covered by `DefaultAuthorizationServiceTest` (11 tests) and
`DefaultAuthorizationPolicyRegistryTest` (6 tests) — see §8.

## 6. Membership-based evaluation

### Why `UserPrincipal` does not carry an organization

A user can hold accepted membership in more than one organization (e.g. running both a
hotel and a car rental business as two separate tenants) — the schema has always allowed
this (`organization.memberships` is unique on `(user_id, organization_id)`, not on
`user_id` alone), but the identity pipeline did not, until this engine's second design
pass. `UserPrincipal` is built once, at JWT-authentication time, before the request has
declared which resource or organization it targets — so it cannot fix a single
"the organization" at that point without being wrong the moment a user has a second
membership.

`UserPrincipal` therefore only carries what is genuinely global to the actor: identity
fields (`subject`, `username`, `email`, …), account status, and **platform-scope** roles/
permissions only (`PLATFORM_OWNER`, `PLATFORM_MANAGER`, `USER` — sourced from
`auth.user_roles`). `tenantUid` remains on the record only because the sealed
`AuthenticatedPrincipal` interface requires every implementation to expose it; for
`UserPrincipal` it is now always `null`.

### Where organization context actually comes from

Everything organization-scoped — which org, which membership, which org-scoped roles
(`ORGANIZATION_OWNER`, `ORGANIZATION_MANAGER`, `STAFF`, sourced from
`organization.membership_roles`) — is resolved **per request**, by
`MembershipResolver.resolve(userUid, organizationId)`. The engine does not decide *which*
organization to resolve against; the caller does, and for every case seen so far that
target organization is unambiguous:

- **Acting on an existing resource** (read/update/cancel/delete): the resource's own
  `ResourceScope.organizationId()`, produced by that resource's `ScopeResolver`. If a
  caller believes they are "currently in" a different organization than the resource
  actually belongs to, that's exactly a cross-organization access attempt — denying it is
  correct, not a bug to work around.
- **Creating a new resource**: whichever organization the create payload targets (e.g. a
  booking's `serviceId` → its owning organization).

A **not-yet-built** case: an action with no specific resource at all (e.g. "list bookings
for my currently-selected organization" on a dashboard). That needs an explicit notion of
"current organization" carried by the session — most likely a JWT claim refreshed by a
dedicated org-switch endpoint, consistent with this project's signed-claim identity model
rather than a client-supplied header. This is a session/auth-flow feature, deliberately
**not** part of this engine — the engine only ever needs "an organization id," it has no
opinion on how the caller decided which one. See §9.

### `evaluatePermission` — the shared RBAC grant check

Every domain policy that performs a plain permission check delegates to
`AuthorizationPolicy.evaluatePermission(context)` (default method) rather than
reimplementing the grant logic:

```java
default AuthorizationDecision evaluatePermission(AuthorizationContext context) {
    if (!(context.principal() instanceof UserPrincipal userPrincipal)) {
        return AuthorizationDecision.abstain(getClass().getSimpleName());
    }
    String slug = context.permissionSlug();          // "<RESOURCE>.<ACTION>"
    boolean granted = userPrincipal.hasPermission(slug)
        || (context.hasMembership() && context.membership().hasPermission(slug));
    return granted
        ? AuthorizationDecision.permit(getClass().getSimpleName())
        : AuthorizationDecision.deny(getClass().getSimpleName(), "MISSING_PERMISSION", ...);
}
```

A grant can come from either source — the actor's platform-scope roles (`PLATFORM_OWNER`/
`PLATFORM_MANAGER` bypass every organization) or their resolved membership for the target
organization. Critically, this **abstains** for any non-`UserPrincipal` actor rather than
denying. Centralizing this on the interface — instead of repeating it per policy — exists
specifically so no future domain policy can accidentally deny a `WidgetPrincipal` outright;
see §7, `WidgetCapabilityPolicy`, for why that specific mistake would be a real regression,
not a theoretical one.

## 7. Wildcard policies

A "wildcard" policy has `resourceType() == AuthorizationPolicy.WILDCARD_RESOURCE_TYPE`
(`"*"`), which `DefaultAuthorizationPolicyRegistry` attaches to the resolved list for
*every* resource type, not just one. These four are the baseline, evaluated before any
domain-specific policy — the layer meant to reject malicious/anomalous requests before
business logic is ever consulted.

| Policy | `order()` | Actor | Purpose |
|---|---|---|---|
| `ActorStatusPolicy` | -20 | both | Deny if the account/widget is not active |
| `WidgetCapabilityPolicy` | -10 | widget only | Fixed allow-list of actions a widget may ever perform |
| `OrganizationIsolationPolicy` | 0 | user only | Deny unless the caller holds an accepted membership in the resource's organization |
| `WidgetTenantIsolationPolicy` | 0 | widget only | Deny unless the resource's tenant matches the widget's own tenant |

### `ActorStatusPolicy`

Runs first because it's the cheapest and most fundamental check. Uses an **exhaustive
`switch`** over the sealed `AuthenticatedPrincipal` (`UserPrincipal → isActive()`,
`WidgetPrincipal → isActive()`), not an `instanceof` chain — if a third permitted type is
ever added to the sealed interface, this fails to *compile* until updated. `DENY
"ACTOR_INACTIVE"` on failure, `ABSTAIN` otherwise.

### `WidgetCapabilityPolicy`

Per `SECURITY.md` §10, `WidgetPrincipal` has no roles/permissions at all — it is a
single-purpose actor with a small, fixed capability set. This policy is that capability
set made real: a hardcoded allow-list of permission slugs (`AVAILABILITY.READ`,
`SERVICE.READ`, `SELECTEDSLOT.CREATE`/`DELETE`, `BOOKING.CREATE`, `ATTENDEE.CREATE`).
`ABSTAIN` for `UserPrincipal` (has nothing to say about users); for `WidgetPrincipal`,
`PERMIT` if the slug is allow-listed, `DENY "WIDGET_CAPABILITY_DENIED"` otherwise. This is
the first policy in the pipeline that can actually `PERMIT`.

### `OrganizationIsolationPolicy`

`ABSTAIN` for non-`UserPrincipal`. `ABSTAIN` for `PLATFORM_OWNER`/`PLATFORM_MANAGER` (these
are deliberately cross-organization roles per their seed grants). Otherwise: `DENY
"OUT_OF_SCOPE"` if the resource's scope is unresolved, or if no membership was resolved, or
if the resolved membership's organization doesn't match the resource's — otherwise
`ABSTAIN`. It **never** `PERMIT`s. That's deliberate: if a same-organization match returned
`PERMIT`, being in the right organization alone would be enough to grant access to *any*
action on *any* resource, since `DefaultAuthorizationService` would set `permitted` on the
very first policy in the list and no RBAC check downstream would ever need to run.
Isolation policies rule requests **out**; only a domain policy rules one **in**.

### `WidgetTenantIsolationPolicy`

The widget-side mirror of the above: `ABSTAIN` for `UserPrincipal`, `DENY "OUT_OF_SCOPE"`
for `WidgetPrincipal` if the resource's tenant is unresolved or doesn't match
`widgetPrincipal.tenantId()`, `ABSTAIN` otherwise. Exists because `OrganizationIsolationPolicy`
has nothing to check for a widget (widgets have no `organizationId()` — they're
tenant-scoped, one tenant per widget, guaranteed non-null per `SECURITY.md` §10), and
without this a stolen/leaked widget key could act cross-tenant with only origin validation
standing in the way.

### Why `WidgetCapabilityPolicy` permitting and a later domain policy denying is a real risk, not a hypothetical

`WidgetCapabilityPolicy` runs at `order = -10` and can `PERMIT` a widget's `BOOKING.CREATE`.
Per §5, a later `DENY` in the same evaluation always overrides that permit. If a booking
domain policy (`BookingCreatePolicy`) called `evaluatePermission` and that method denied
non-`UserPrincipal` actors instead of abstaining, it would silently override the widget's
already-granted permission — breaking the widget booking flow with no code anywhere
*looking* wrong in isolation. This is exactly why the grant check is centralized in
`evaluatePermission` rather than reimplemented per policy: getting the abstain/deny
distinction right once, in one place, is what makes it impossible to get wrong per domain.

## 8. Domain policy pattern (reference implementation: `BOOKING`)

Convention going forward: **one policy class per action**, not one class per resource.
`core/booking/authorization/` is the reference:

- `BookingScopeResolver` — `ScopeResolver<BookingEntity>`. Walks
  `booking.getService().getOrganization()` for `organizationId`, then
  `TenantRepository.findByOrganizationOrganizationId(...)` for `tenantId` (nullable —
  most organizations have no tenant row yet). Populates **both** fields on one
  `ResourceScope`, since the resolver has to serve both `OrganizationIsolationPolicy` and
  `WidgetTenantIsolationPolicy` from the same resolved scope.
- `BookingCreatePolicy` / `BookingReadPolicy` / `BookingUpdatePolicy` / `BookingDeletePolicy`
  / `BookingCancelPolicy` / `BookingRejectPolicy` — each `resourceType() = "BOOKING"`,
  `supports(action)` narrowed to exactly one of the six real, seeded permission slugs
  (`BOOKING.CREATE/READ/UPDATE/DELETE/CANCEL/REJECT` — `core/V83__core_seed_permission.sql`),
  `order() = 10`, `evaluate()` is a one-line delegation to `evaluatePermission(context)`.
  `CANCEL`/`REJECT` have no live controller endpoint yet, but the permission slugs and role
  grants already exist, so the policies exist ahead of the endpoints — the same
  forward-reservation pattern `AGENTS.md` documents for `SESSION.*` permissions.
- `BookingOwnershipPolicy` — the one policy in this domain that is **not** plain RBAC.
  `supports(action)` limited to `UPDATE`/`CANCEL` only (the two actions where `STAFF` holds
  a grant broad enough to need narrowing — `STAFF` never holds `BOOKING.DELETE`/`REJECT` at
  all, so this policy would never fire there). Logic: abstain unless the caller's *resolved
  membership* (not `UserPrincipal` — `STAFF` is an organization-scoped role) includes
  `STAFF`; if so, deny (`"NOT_ASSIGNED_BOOKING"`) unless the booking's host
  (`booking.getUser().getUserUid()`) is the caller. Deliberately does not restrict `READ` —
  shared team-calendar visibility was assumed as the default, restricting only mutation.

This demonstrates the full three-tier shape every future domain should follow: wildcard
gates (§7) → per-action RBAC (`evaluatePermission`) → any resource-specific narrowing
(ownership, status preconditions, etc.) as its own, separately-scoped policy class.

## 9. Test coverage

27 tests, added specifically to verify the engine's own combination logic rather than any
one policy's business rules (Mockito-based, no database — consistent with this project's
existing test convention):

| File | Count | Proves |
|---|---|---|
| `DefaultAuthorizationPolicyRegistryTest` | 6 | Wildcard/specific merge, `order()` sort is real (tested against scrambled injection order), `supports(action)` filtering, resource-type isolation, empty-registry → empty list, no double-counting when `resourceType == "*"` |
| `DefaultAuthorizationServiceTest` | 11 | Permit/deny/no-policy-deny, `DENY` short-circuits (proven via `verify(..., never())`, not just result inspection), a later `DENY` overrides an earlier `PERMIT`, first `PERMIT` wins among multiple permits, `authorize()` never returns `ABSTAIN` as its outward effect, a policy's unchecked exception becomes `PolicyEvaluationException`, `require()` throws/doesn't-throw correctly |
| `DefaultMembershipResolverTest` | 5 | Null-guarded without touching the repository, empty on no accepted membership, correct role/permission flattening, permission dedup across two roles granting the same slug |
| `DefaultScopeResolverRegistryTest` | 5 | Null → unscoped, no-match → unscoped, exact-type dispatch, **proxy-safe dispatch** (a hand-built subclass still resolves — proof `isInstance` is used, not exact-`Class`-equality, which would silently break on every real Hibernate lazy-loaded entity), duplicate-resource-type registration fails at `@PostConstruct`

**Explicit limitation**: `DefaultMembershipResolver` and `BookingScopeResolver`'s Spring
Data **derived queries** (`findByUserUserUidAndOrganizationOrganizationIdAndAccepted`,
`findByOrganizationOrganizationId`) are validated only by `ServerApplicationTests`' full
context load passing — that proves Spring could construct valid JPQL from the method
names, not that the queries are semantically correct against real data. Closing that gap
would mean introducing a `@DataJpaTest`/embedded-database test category this project has
not used anywhere else; left as a deliberate, stated gap rather than done silently.

## 10. Known problems and open flags

Ranked by what would actually bite first.

1. **Nothing outside `security/authorization/` calls this engine yet.** No controller or
   `*ServiceImpl` anywhere in the codebase calls `AuthorizationService.authorize(...)` or
   `.require(...)` — confirmed by a full-repo grep. Every domain in this codebase, `BOOKING`
   included, is still enforced only by "is authenticated" at the `SecurityConfig` level.
   The engine is complete and tested; it is not yet load-bearing for a single live request.
2. **`docs/security/ROLES_DESIGN.md` is stale and now actively misleading.** It documents
   business-level roles as `BUSINESS_OWNER` / `BUSINESS_MANAGER` / `STAFF` / `MEMBER`. The
   real seeded roles (`auth/V89__seed_roles.sql`) are `ORGANIZATION_OWNER` /
   `ORGANIZATION_MANAGER` / `STAFF` — no `MEMBER` role exists at all, and the naming itself
   differs (`ORGANIZATION_*`, not `BUSINESS_*`). `OrganizationIsolationPolicy` and
   `BookingOwnershipPolicy` were built against the real seeded names; if that document is
   ever treated as authoritative, code and docs will actively disagree.
3. **No "current organization" mechanism exists for resource-less actions.** §6 covers
   this: any action tied to a specific resource resolves its organization from that
   resource, but a list/dashboard-style action with no single resource has nothing to
   resolve membership against yet. Needs a deliberate session-level decision (most likely a
   JWT claim refreshed on org-switch), out of this engine's scope by design, not built.
4. **No caching on the request-time resolution path.** Every authorized action now
   potentially costs: the identity loader (already ran once for the whole request, per
   `JwtAuthenticationFilter`) + a `ScopeResolver` lookup (may hit `TenantRepository`) + a
   `MembershipResolver` lookup (two queries: membership, then its role assignments). None
   of this is cached. Redis is already provisioned in this project and was flagged as the
   right mechanism for this back when the engine was first scoped — not yet built.
5. **`BookingRequest.userId` (the host) is still client-suppliable with no
   organization-membership check at `CREATE` time.** Neither `BookingCreatePolicy` (checks
   the verb, not the payload) nor `BookingOwnershipPolicy` (explicitly abstains on `CREATE`,
   nothing to own yet) catches a request that names a host user in a different
   organization than the target service. Flagged when `BookingCreatePolicy` was designed;
   still open. A narrow addition to `BookingCreatePolicy` — confirm the request's `userId`
   shares an organization with `serviceId` — would close it; not built.
6. **The widget capability allow-list (`WidgetCapabilityPolicy`) is a hardcoded `Set<String>`
   in code**, not data-driven from `auth.permissions`. Adding a new widget-usable action
   requires a code change and redeploy, not a seed migration. Acceptable for the current
   fixed, narrow widget capability set; worth reconsidering if that set grows much further.
7. **No audit-trail wiring.** `AuthorizationDecision` already carries everything
   `audit.audit_events` would want (`policyName`, `code`, `reason`) — SECURITY.md's own
   framing of Policy/Audit as related-but-distinct IAM concerns anticipated this — but
   nothing currently writes a denial or grant into the audit schema. A denied request today
   is only visible as an HTTP 403/404 response, not as a queryable record.
8. **`UserPrincipal.tenantUid` is now unconditionally `null`.** This is correct per §6, but
   it further loosens `SECURITY.md` §5's "Tenant Optionality" section, which already
   documents `tenantUid` as nullable "because a user hasn't subscribed to a plan yet" — that
   framing no longer fully describes *why* it's null for a signed-in user (it's null
   because organization context isn't fixed at authentication time at all now, independent
   of plan subscription). Worth a coordinated update alongside the other stale-doc flags
   `SECURITY.md`/`SERVER_AGENTS.md` already carry, not urgent on its own.
9. **`SERVER_AGENTS.md`'s note that `DefaultUserIdentityLoader.build()` resolves membership
   and profile via `orElseThrow`** no longer describes the code. That loader no longer
   queries membership or profile at all — both are now resolved per-request elsewhere, not
   at authentication time. Same category as flag 8: a doc describing a now-superseded
   design, not a code defect.
