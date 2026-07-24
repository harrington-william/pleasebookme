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

5. Component Responsibilities

IdentityLoader

IdentityLoader is responsible for assembling all information required to authenticate a user.

Typical responsibilities include:

Loading User
Loading Membership
Loading Organization
Loading Tenant
Loading Profile
Loading Roles
Loading Permissions

IdentityLoader is the only component responsible for querying repositories during authentication.

It does not perform authentication decisions or authorization.

AuthenticationAggregate

AuthenticationAggregate is a read model used exclusively during authentication.

It groups together all information required to construct an authenticated identity.

It is not a Domain-Driven Design Aggregate.

Instead, it is an authentication-specific projection that minimizes repository interactions throughout the authentication pipeline.

PrincipalMapper

PrincipalMapper transforms AuthenticationAggregate into AuthenticatedPrincipal.

It contains no repository access.

It performs only object transformation.

Because it is isolated from persistence, it remains reusable across different authentication mechanisms.

AuthenticatedPrincipal

AuthenticatedPrincipal is the canonical representation of an authenticated actor.

It belongs entirely to the Authentication domain.

It contains identity information such as:

Actor Type
User Identifier
Tenant
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

It intentionally contains no framework-specific interfaces.

This object is consumed by the authorization engine throughout the application.

GrantedAuthorityAdapter

Spring Security authorizes requests using GrantedAuthority.

GrantedAuthorityAdapter converts business concepts such as:

Role
Permission

into

GrantedAuthority

No business logic exists inside this adapter.

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

For this reason, PrincipalUserDetails is wrapped inside Spring Authentication implementations such as:

UsernamePasswordAuthenticationToken

The SecurityContext therefore stores framework-specific objects.

However, the business layer never interacts with those objects directly.

Instead, infrastructure components extract the underlying AuthenticatedPrincipal before entering business services.

Consequently, business code remains completely unaware of Spring Security.

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

9. Architectural Benefits

This architecture provides several long-term advantages.

Framework Independence

Business logic remains isolated from Spring Security.

Changing authentication frameworks requires modifications only within the adapter layer.

Single Responsibility

Each component performs one well-defined responsibility.

Loading, mapping, adaptation, and authorization remain independent concerns.

Extensibility

Future authentication mechanisms—including OAuth, API Keys, Widget Tokens, and Service Accounts—can reuse the same architecture by introducing new loaders and mappers while continuing to produce the same AuthenticatedPrincipal.

Testability

Business authorization can be unit tested using AuthenticatedPrincipal without constructing SecurityContextHolder or mocking Spring Security.

Maintainability

Framework code remains localized within infrastructure.

Business code operates exclusively on domain concepts, reducing coupling and simplifying long-term evolution.

10. Future Evolution

This identity architecture serves as the foundation for all future authentication mechanisms within the platform.

The next architectural layer builds upon this design by introducing JWT-based authentication.

JWT authentication will not replace AuthenticatedPrincipal.

Instead, JWT becomes an additional mechanism for reconstructing the same canonical authenticated identity on every request.

Consequently, regardless of whether authentication originates from Username/Password, JWT, OAuth, API Keys, or Widget Tokens, the remainder of the platform continues to operate exclusively on AuthenticatedPrincipal.