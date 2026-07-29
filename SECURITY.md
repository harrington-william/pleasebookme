# Authentication Identity & Principal Architecture

1. Overview

The Authentication Identity architecture defines how authenticated identities are represented throughout the platform while maintaining a strict separation between the business domain and the underlying security framework.

The primary architectural objective is to ensure that the Authentication domain owns all business concepts related to identity, while Spring Security remains an infrastructure concern responsible only for integrating with the application framework.

This design prevents framework-specific abstractions from leaking into business logic and allows the platform to evolve independently of Spring Security.

2. Motivation

Spring Security requires authenticated identities to implement framework-specific contracts such as UserDetails and Authentication.

Although these abstractions are useful for integrating with the framework, they should not become part of the domain model because they tightly couple business logic to a specific security implementation.

Instead, the platform introduces a canonical business identity called AuthenticatedPrincipal.

Every authentication mechanism—including Username/Password, JWT, OAuth, API Keys, Widgets, and future Service Accounts—ultimately produces the same authenticated identity model.

Business authorization decisions are always performed using AuthenticatedPrincipal rather than Spring Security interfaces.
****
3. Architectural Principles

The identity architecture follows several core principles.

Framework Isolation

Business code must never depend directly on Spring Security classes.

Only the infrastructure layer is aware of:

UserDetails
Authentication
GrantedAuthority
SecurityContextHolder

The business layer only understands:

AuthenticatedPrincipal
AuthenticationAggregate
Authorization Context
Canonical Identity Model

The platform defines exactly one authenticated identity representation.

AuthenticatedPrincipal

This object represents the authenticated actor regardless of how authentication occurred.

Examples include:

Username & Password
JWT
Google OAuth
API Key
Widget Token
Internal Service Account

Every authentication mechanism ultimately produces the same identity representation.

In practice, AuthenticatedPrincipal is a sealed interface, and each actor type is a concrete record implementing it — UserPrincipal, WidgetPrincipal, and future additions such as ApiKeyPrincipal. The sealed permits list is what enforces the "exactly one representation" guarantee at compile time: every actor must implement the interface's common contract (actorType, subject, tenantUid), and any code that switches over AuthenticatedPrincipal is checked exhaustively by the compiler, so introducing a new actor type surfaces every call site that needs to handle it. Every implementation is guaranteed to expose a tenantUid field, but not every implementation guarantees it is non-null — see Tenant Optionality below.

Not every actor carries the same amount of identity data. UserPrincipal is rich — username, roles, permissions, organization, membership, profile — because users participate in RBAC and organizational structure. WidgetPrincipal is deliberately minimal — actor type, widget identifier, tenant, status — because a widget has no roles layer, just a fixed, narrow capability set bound to a single tenant. The sealed interface only guarantees the fields every actor needs (identity, tenant scoping); anything beyond that is specific to the concrete type.

Separation of Responsibilities

Authentication is divided into multiple independent responsibilities.

Component	Responsibility
IdentityLoader	Load authentication data from persistence
AuthenticationAggregate	Authentication read model
PrincipalMapper	Convert aggregate into business identity
AuthenticatedPrincipal	Canonical authenticated identity
GrantedAuthorityAdapter	Convert business roles into Spring authorities
UserDetailsAdapter	Adapt business identity into Spring UserDetails
PrincipalUserDetails	Spring Security adapter

Each component performs exactly one responsibility.

4. Authentication Pipeline

The complete authentication pipeline is illustrated below.

```bash
Database
        │
        ▼
IdentityLoader
        │
        ▼
AuthenticationAggregate
        │
        ▼
PrincipalMapper
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
Authentication
        │
        ▼
SecurityContextHolder

Notice that the business identity is created before interacting with Spring Security.

The framework only receives an adapter around the business identity.
```

This is the full pipeline for actors whose identity requires joining multiple tables — currently only User (user, membership, organization, tenant, profile, roles, permissions). Not every actor needs every stage. Widget authentication uses a shortened pipeline instead:

```bash
Database
        │
        ▼
WidgetIdentityLoader
        │
        ▼
WidgetPrincipal
        │
        ▼
AuthenticationTokenFactory
        │
        ▼
SecurityContextHolder
```

WidgetIdentityLoader queries a single entity (WidgetEntity) and constructs WidgetPrincipal directly — there is no AuthenticationAggregate or PrincipalMapper stage, because there is no multi-repository join to justify a separate read-model/transformation split. This is a deliberate simplification, not an incomplete implementation: the AuthenticationAggregate/PrincipalMapper stages exist to keep a complex, multi-entity assembly out of the loader, and a single-entity actor has nothing for them to do. Future low-complexity actors (e.g. API Keys, if their identity resolves from one row) should follow the same shortened shape rather than introducing an aggregate/mapper pair with nothing to aggregate or map.

5. Component Responsibilities

IdentityLoader

IdentityLoader is responsible for assembling all information required to authenticate a user.

Typical responsibilities include:

Loading User
Loading Membership
Loading Organization
Loading Tenant (best-effort — see Tenant Optionality under AuthenticatedPrincipal below)
Loading Profile
Loading Roles
Loading Permissions

IdentityLoader is the only component responsible for querying repositories during authentication.

It does not perform authentication decisions or authorization.

Each actor type has its own IdentityLoader (UserIdentityLoader, WidgetIdentityLoader). For a single-entity actor, the loader is also where non-identity, pre-authentication gating happens — for example, WidgetIdentityLoader validates that the request's origin matches the widget's registered origin before it will construct a principal at all, since a failed origin check should never produce a WidgetPrincipal in the first place. This gate is not authorization (it doesn't ask what the actor may do) and it doesn't belong on the principal itself (it's a one-time admission check, not an ongoing identity property); it lives in the loader because IdentityLoader already owns "decide whether this actor can be resolved from persistence at all."

AuthenticationAggregate

AuthenticationAggregate is a read model used exclusively during authentication.

It groups together all information required to construct an authenticated identity.

It is not a Domain-Driven Design Aggregate.

Instead, it is an authentication-specific projection that minimizes repository interactions throughout the authentication pipeline.

Currently only User goes through an AuthenticationAggregate. Widget's identity is a single entity, so WidgetIdentityLoader builds WidgetPrincipal directly with no intermediate aggregate — see the shortened pipeline in the Authentication Pipeline section above.

PrincipalMapper

PrincipalMapper transforms AuthenticationAggregate into AuthenticatedPrincipal.

It contains no repository access.

It performs only object transformation.

Because it is isolated from persistence, it remains reusable across different authentication mechanisms.

PrincipalMapper is a generic interface (PrincipalMapper<T>); UserPrincipalMapper is its only current implementation. Widget has no PrincipalMapper, for the same reason it has no AuthenticationAggregate — there is nothing to transform beyond what the loader already assembled.

AuthenticatedPrincipal

AuthenticatedPrincipal is the canonical representation of an authenticated actor.

It belongs entirely to the Authentication domain.

It is a sealed interface (permits UserPrincipal, WidgetPrincipal, ...), guaranteeing every implementation exposes:

Actor Type
Subject (a unique identifier for the actor)
Tenant field (present on every implementation; not guaranteed non-null on every implementation — see Tenant Optionality below)

Beyond that common contract, each concrete type carries whatever identity information is relevant to it. UserPrincipal carries the rich set:

Subject
Tenant (nullable — see Tenant Optionality below)
Organization
Membership
Profile
Username
Display Name
Locale
Timezone
Account Status
Roles
Permissions

WidgetPrincipal carries only what a widget actually has: Widget Identifier, Tenant, Status. It has no roles or permissions — widgets are single-purpose actors with a fixed, narrow capability set bound to one tenant, not participants in RBAC.

It intentionally contains no framework-specific interfaces.

This object is consumed by the authorization engine throughout the application.

Tenant Optionality

tenantUid is guaranteed to exist as a field on every AuthenticatedPrincipal implementation, but it is not guaranteed to be non-null for every actor.

WidgetPrincipal's tenantUid is always populated. A widget is never self-provisioned — it is manually created for a business that is already a paying tenant, so a WidgetPrincipal without a tenant cannot exist under the current onboarding model.

UserPrincipal's tenantUid is nullable. A newly registered user receives an Organization, a self-accepted Membership, and a Profile at registration time (see Self-Serve Registration below), but no Tenant. Tenant represents an active subscription — plan, ecosystem, region, quotas — and is only created once the user's organization actually subscribes to a plan. This lets the platform support a free, no-plan-required entry point (register → get an organization → use the system) without forcing every user through paid-tenant provisioning, while Widget — always issued to an already-paying business — keeps its stronger guarantee.

DefaultUserIdentityLoader.build() reflects this asymmetry directly: Membership and Profile are resolved with orElseThrow (a user missing either is a data-integrity error, since register() always creates both), while Tenant is resolved with orElse(null) (a user without one simply hasn't subscribed to a plan yet, which is an expected, common state, not an error).

Any code reading principal.tenantUid() off a UserPrincipal to make a tenant-scoped decision (gating a paid feature, enforcing a plan quota, etc.) must null-check it and treat null as "no active plan" — it must not assume every authenticated user has a tenant. Code operating on a WidgetPrincipal can continue to assume tenantUid is always present.

This is a deliberate loosening of an earlier assumption: tenantUid was originally treated as guaranteed non-null for every actor, which held while Widget was the only actor type actually exercising the tenant relationship (widgets were manually provisioned per paying tenant, one at a time). It stops holding once User can self-register without becoming a paying tenant first, which is the direction the platform is evolving toward — plan subscription (Free, Pro, etc.) becoming optional rather than a prerequisite of having an account at all.

Self-Serve Registration

Registration (AuthServiceImpl.register()) creates a User, a UserPassword, a USER role assignment, an Organization named after the user, a Membership linking the user to that organization with accepted = true (self-membership needs no invite/acceptance step), and a Profile. No Tenant is created. Once these exist, register() runs the same UserIdentityLoader → UserPrincipalMapper → JwtEngine pipeline as login and token refresh to issue an access/refresh token pair immediately — registration is a complete authentication event, not just an account-creation step that requires a separate login call afterward.

GrantedAuthorityAdapter

Spring Security authorizes requests using GrantedAuthority.

GrantedAuthorityAdapter converts business concepts such as:

Role
Permission

into

GrantedAuthority

No business logic exists inside this adapter.

GrantedAuthorityAdapter, UserDetailsAdapter, and PrincipalUserDetails (below) are all UserPrincipal-specific. Widget authentication does not use any of the three — see SecurityContext Integration.

UserDetailsAdapter

UserDetailsAdapter converts AuthenticatedPrincipal into PrincipalUserDetails.

It represents the boundary between the business identity model and Spring Security.

PrincipalUserDetails

PrincipalUserDetails implements Spring Security's UserDetails interface.

Its only responsibility is allowing Spring Security to interact with the application's business identity.

It should never contain business rules.

It simply delegates to AuthenticatedPrincipal.

6. SecurityContext Integration

Spring Security requires an Authentication object to be stored inside SecurityContextHolder.

For User, PrincipalUserDetails is wrapped inside Spring Authentication implementations such as:

UsernamePasswordAuthenticationToken

Widget does not go through PrincipalUserDetails at all. UsernamePasswordAuthenticationToken and the UserDetails contract it wraps encode username/password authentication semantics — a password hash, an account-locked flag — that do not apply to a widget authenticated via public/secret key plus origin validation. Instead, WidgetPrincipal is stored directly as the principal on a PreAuthenticatedAuthenticationToken, which Spring Security provides specifically for actors already authenticated by a mechanism outside the standard username/password filter chain.

AuthenticationTokenFactory is the single component responsible for this decision. It accepts any AuthenticatedPrincipal and switches on the concrete sealed type — UserPrincipal produces a UsernamePasswordAuthenticationToken via UserDetailsAdapter/PrincipalUserDetails as described above, WidgetPrincipal produces a PreAuthenticatedAuthenticationToken directly. Because AuthenticatedPrincipal is sealed, this switch is exhaustive: adding a new actor type without adding its branch here is a compile error, not a silent gap.

The SecurityContext therefore stores framework-specific objects, and the concrete shape of that object differs by actor type — a UserDetails-wrapping token for User, a bare-principal token for Widget. However, the business layer never interacts with those objects directly. Instead, infrastructure components extract the underlying AuthenticatedPrincipal before entering business services. Code that reads the principal back out of Authentication.getPrincipal() must handle both shapes (PrincipalUserDetails.getUserPrincipal() for User, or the AuthenticatedPrincipal directly for Widget) — there is no single unwrap path yet, since no code outside the security package has needed to do this extraction so far.

Consequently, business code remains completely unaware of Spring Security.

JwtAuthenticationFilter is the entry point that ties this together for JWT-bearing requests. JwtClaims carries an actorType field (populated by JwtClaimsFactory, itself generalized to accept any AuthenticatedPrincipal — see Dependency Direction) alongside a generic subject/tenant pair, so a single JWT shape serves every actor type. On each request, the filter verifies the token, branches on claims.actorType() to call the matching IdentityLoader, and hands the resulting AuthenticatedPrincipal to AuthenticationTokenFactory before storing the result in SecurityContextHolder.

7. Authorization Boundary

Authentication and authorization are intentionally separated.

Authentication answers:

Who is the current actor?

Authorization answers:

Is this actor allowed to perform the requested operation?

Authentication produces:

AuthenticatedPrincipal

Authorization consumes:

AuthenticatedPrincipal

Spring Security participates only during authentication and request integration.

Business authorization policies never depend on Spring Security APIs.

8. Dependency Direction

Dependencies flow only in one direction.

AuthenticatedPrincipal
        │
        ▼
PrincipalUserDetails
        │
        ▼
Authentication
        │
        ▼
SecurityContextHolder

The reverse dependency is intentionally prohibited.

AuthenticatedPrincipal has no knowledge of:

UserDetails
Authentication
GrantedAuthority
SecurityContextHolder

This ensures that the Authentication domain remains portable, testable, and independent of any particular security framework.

The diagram above is User's path. Widget skips the PrincipalUserDetails step entirely (AuthenticatedPrincipal → Authentication → SecurityContextHolder) — see SecurityContext Integration. Either way, AuthenticatedPrincipal itself has no knowledge of the framework types downstream of it, regardless of which path a given actor takes.

9. Architectural Benefits

This architecture provides several long-term advantages.

Framework Independence

Business logic remains isolated from Spring Security.

Changing authentication frameworks requires modifications only within the adapter layer.

Single Responsibility

Each component performs one well-defined responsibility.

Loading, mapping, adaptation, and authorization remain independent concerns.

Extensibility

Future authentication mechanisms—including OAuth, API Keys, and Service Accounts—can reuse the same architecture by introducing a new IdentityLoader (and, only if the actor's identity requires joining multiple tables, an AuthenticationAggregate/PrincipalMapper pair) while continuing to produce the same AuthenticatedPrincipal contract. Widget Tokens are already implemented this way — see Widget Authentication.

Testability

Business authorization can be unit tested using AuthenticatedPrincipal without constructing SecurityContextHolder or mocking Spring Security.

Maintainability

Framework code remains localized within infrastructure.

Business code operates exclusively on domain concepts, reducing coupling and simplifying long-term evolution.

10. Widget Authentication

Widget authentication is the second concrete authentication mechanism built on this architecture, after User, and the first to deliberately diverge from the full pipeline described above.

A widget authenticates using a public_key/secret_key pair issued at widget registration, rather than a username and password. Unlike a user, a widget has no roles, no permissions, and no organizational membership — it is a single-purpose actor with a fixed, narrow capability set, bound to exactly one tenant. WidgetPrincipal reflects this: it carries only actor type, widget identifier, tenant, and status, with no roles/permissions fields.

Origin Validation

Each widget is registered against exactly one client-owned website domain (e.g. barbershop.com) in widget_origins. This is a defense against a stolen or leaked public key being replayed from a domain the widget was never embedded on — browsers set the Origin header on outbound requests in a way page JavaScript cannot override, so comparing it against the widget's registered origin is a meaningful check against unauthorized cross-origin use of a public key, even though it does not defend against a server-side attacker who can set arbitrary headers.

This check happens inside WidgetIdentityLoader.loadByPublicKey(publicKey, origin), before a WidgetPrincipal is ever constructed: if the widget has origin validation enabled, the loader looks up whether the request's origin is registered for that widget and throws WidgetOriginMismatchException (mapped to 401) on a mismatch. Origin is not a field on WidgetPrincipal — it is a one-time admission check performed by the loader, not an ongoing identity property carried forward on the principal, since nothing downstream of authentication needs to know what origin a request arrived on.

Shortened Pipeline

WidgetIdentityLoader returns WidgetPrincipal directly — there is no WidgetAggregation and no WidgetPrincipalMapper. Both existed briefly during development and were removed once it became clear they added a layer of indirection around a single WidgetEntity lookup with no actual aggregation or transformation work to do. See the Authentication Pipeline and Component Responsibilities sections above for the general principle this establishes for future low-complexity actors.

SecurityContext Integration

WidgetPrincipal never passes through UserDetailsAdapter or PrincipalUserDetails. AuthenticationTokenFactory constructs a PreAuthenticatedAuthenticationToken directly around the WidgetPrincipal instead, since Widget has no password/account-lock concepts for UserDetails to represent. See SecurityContext Integration above for how AuthenticationTokenFactory picks between the two paths.

JWT

JwtClaims and JwtClaimsFactory are actor-agnostic (actorType, subject, tenant, tokenId, tokenType, timestamps), so the same JWT shape and issuance/verification code serves both User and Widget. JwtAuthenticationFilter reads claims.actorType() on every request to decide whether to resolve the subject through UserIdentityLoader or WidgetIdentityLoader.

Not Yet Implemented

WidgetPrincipal currently has no scopes or authority set — a widget authenticates successfully but AuthenticationTokenFactory grants it an empty GrantedAuthority collection. A capability model (e.g. reusing the auth.permissions slug vocabulary as a fixed, non-RBAC scope set per widget) is expected but not yet built. The widget bootstrap flow (the endpoint that exchanges public_key/secret_key plus an Origin header for a JWT, i.e. the actual caller of WidgetIdentityLoader.loadByPublicKey) also does not exist yet — only the JWT-verification path (loadByUid, used by JwtAuthenticationFilter) is wired up end to end.

11. Future Evolution

This identity architecture serves as the foundation for all future authentication mechanisms within the platform.

JWT-based authentication and Widget authentication are both now implemented on top of it, per the sections above.

JWT did not replace AuthenticatedPrincipal — it is a mechanism for reconstructing the same canonical authenticated identity on every request, verified once per request by JwtAuthenticationFilter and handed to the same AuthenticationTokenFactory/SecurityContextHolder integration regardless of actor type.

Remaining future work follows the same shape: API Keys and Service Accounts are added by introducing a new IdentityLoader (and only an AuthenticationAggregate/PrincipalMapper pair if the actor's identity genuinely requires a multi-table join), adding the new concrete type to AuthenticatedPrincipal's permits list, and adding its branch to AuthenticationTokenFactory's switch — the compiler enforces that the last two steps aren't skipped. Regardless of whether authentication originates from Username/Password, JWT, OAuth, API Keys, or Widget Tokens, the remainder of the platform continues to operate exclusively on AuthenticatedPrincipal.