# Domain Architecture

The platform is organized into multiple architectural domains that separate responsibilities according to business capabilities rather than technical implementation details. Each domain owns its own business logic, persistence model, security policies, and operational responsibilities.

Although all domains currently execute within a centralized Spring Boot application and share the same PostgreSQL instance, they are intentionally designed as independent modules with clear ownership boundaries. This architectural separation minimizes coupling, improves maintainability, and prepares the platform for future service decomposition without requiring significant domain redesign.

  

## 11 Domains

  

- Core

- Auth

- Resource

- Tenant

- Widget

- Integration

- Audit

- Webhook

- Notification

- Analytics

- Billing

  

---

  

# Core Domain

  

## Purpose

  

The Core domain represents the heart of the reservation platform. It contains the business rules responsible for reservation management, resource allocation, availability computation, booking policies, and customer interactions.

  

Every business capability ultimately depends on the Core domain because it represents the platform's primary source of business value.

  

Unlike other domains, the Core domain is intentionally unaware of infrastructure concerns such as authentication providers, notification systems, analytics, or deployment architecture. Its responsibility is exclusively the execution of reservation-related business rules.

  

---

  

## Responsibilities

  

The Core domain is responsible for:

  

- Reservation lifecycle management

- Resource management

- Booking policy enforcement

- Availability computation

- Schedule generation

- Conflict detection

- Reservation validation

- Customer management

- Business service configuration

- Reservation state transitions

- Business rule execution

  

Typical entities include:

  

- Organization

- Reservation

- Resource

- Booking Policy

- Availability Rule

- Schedule

- Customer

- Service

- Reservation Event

  

---

  

## Dependencies

  

The Core domain depends only on platform capabilities required to execute reservations.

  

Dependencies include:

  

- Authentication Domain (current authenticated user)

- Tenant Domain (tenant ownership)

- Integration Domain (external synchronization)

- Audit Domain (business event recording)

  

The Core domain never directly depends on notification providers, SMS vendors, Google APIs, or analytics systems.

  

---

  

## Scaling Strategy

  

The Core domain is expected to become the highest-traffic domain in the platform.

  

Scaling priorities include:

  

- Horizontal application scaling

- Redis caching

- Database indexing

- Read optimization

- Optimistic locking

- Reservation concurrency control

- Stateless API deployment

  

Future evolution may separate this domain into its own Reservation Service while preserving its public API contract.

  

---

  

## Security

  

The Core domain enforces business authorization rather than identity authentication.

  

Examples include:

  

- Resource ownership validation

- Reservation permissions

- Tenant isolation

- Customer ownership

- Booking policy enforcement

- Availability constraints

  

Authentication is delegated to the Authentication Domain.

  

---

  

## Policies

  

The Core domain defines business policies including:

  

- Reservation policies

- Booking duration rules

- Resource allocation rules

- Cancellation policies

- Rescheduling policies

- Buffer time rules

- Capacity validation

- Availability computation

  

Business policies should never be implemented inside controllers or infrastructure services.

  

---

  

## Ownership

  

The Core domain owns:

  

- Reservation lifecycle

- Business rules

- Reservation data model

- Resource model

- Customer model

- Availability model

- Booking policies

  

No other domain may modify reservation state directly.

  

---

  

# Auth Domain

  

## Purpose

  

The Authentication Domain manages platform identity and access management.

  

Its responsibility is determining **who** is interacting with the platform and **what permissions** they possess.

  

Unlike the Core domain, it does not understand reservation business rules.

  

---

  

## Responsibilities

  

The Authentication Domain manages:

  

- User accounts

- Authentication

- Authorization

- Roles

- Permissions

- RBAC

- Session management

- OAuth identities

- API authentication

- Widget authentication

  

Typical entities include:

  

- Role

- Permission

- OAuth Account

- Refresh Token

- API Key

- Widget Token

  

---

  

## Dependencies

  

The Authentication Domain should have minimal dependencies.

  

It may interact with:

  

- Tenant Domain

- Integration Domain (OAuth)

  

It should not depend on the Core domain.

  

Identity should remain reusable across multiple platform capabilities.

  

---

  

## Scaling Strategy

  

Authentication is read-heavy.

  

Scaling approaches include:

  

- JWT authentication

- Redis session cache

- Permission caching

- Stateless authentication

- Token validation optimization

  

---

  

## Security

  

This domain represents the platform's primary security boundary.

  

Responsibilities include:

  

- Identity verification

- Password management

- OAuth validation

- JWT generation

- API authentication

- Widget authentication

- Credential protection

  

---

  

## Policies

  

Authentication policies include:

  

- Password policy

- MFA policy

- Token expiration

- Session lifetime

- API key policy

- OAuth scopes

- Widget trust policy

  

---

  

## Ownership

  

The Authentication Domain exclusively owns:

  

- Identity

- Credentials

- Roles

- Permissions

- Authentication state

  

No other domain stores user credentials.

  

---

  

# Tenant Domain

  

## Purpose

  

The Tenant Domain provides logical isolation between organizations sharing the same infrastructure.

  

Every business using the platform operates as an independent tenant while sharing application resources.

  

---

  

## Responsibilities

  

Responsibilities include:

  

- Organization management

- Tenant lifecycle

- Subscription ownership

- Resource ownership

- Tenant configuration

- Feature flags

- Platform plans

- Data isolation

  

Typical entities include:

  

- Tenant

- Organization

- Subscription

- Plan

- Feature

- Configuration

  

---

  

## Dependencies

  

The Tenant Domain interacts with:

  

- Authentication Domain

- Core Domain

- Integration Domain

  

Most platform requests eventually resolve tenant ownership before executing business logic.

  

---

  

## Scaling Strategy

  

Tenant data should support:

  

- Multi-tenancy

- Tenant partitioning

- Future database sharding

- Regional deployment

- Geographic expansion

  

The architecture should allow tenant migration without affecting application behavior.

  

---

  

## Security

  

Responsibilities include:

  

- Tenant isolation

- Cross-tenant protection

- Organization ownership

- Feature authorization

  

Every request should resolve tenant context before accessing business data.

  

---

  

## Policies

  

Examples include:

  

- Tenant provisioning

- Subscription limits

- Resource quotas

- Storage limits

- User limits

- Feature availability

  

---

  

## Ownership

  

The Tenant Domain owns:

  

- Organizations

- Tenant configuration

- Subscription plans

- Feature entitlements

- Tenant metadata

  

---

  

# Integration Domain

  

## Purpose

  

The Integration Domain connects the platform with external systems while preventing external dependencies from leaking into business logic.

  

It serves as the platform's boundary to third-party services.

  

---

  

## Responsibilities

  

Responsibilities include:

  

- Google OAuth2

- Google Calendar

- Webhooks

- External API clients

  

Typical entities include:

  

- OAuth connection

- Integration

- Webhook

- Provider

  

---

  

## Dependencies

  

The Integration Domain depends on:

  

- Authentication Domain

- Core Domain

- Tenant Domain

  

External providers never communicate directly with the Core domain.

  

---

  

## Scaling Strategy

  

Integration workloads are asynchronous.

  

Future scaling includes:

  

- Rate limiting

- Provider abstraction

- Circuit breakers

  

---

  

## Security

  

Security concerns include:

  

- OAuth tokens

- API secrets

- Provider credentials

- Request signatures

- Secret rotation

  

Sensitive credentials should never be stored outside this domain.

  

---

  

## Policies

  

Policies include:

  

- Retry strategy

- Timeout policy

- Rate limiting

- Provider fallback

- Synchronization frequency

  

---

  

## Ownership

  

The Integration Domain owns:

  

- Provider communication

- OAuth connections

- Third-party synchronization

  

---

  

# Notification Domain

  

## Purpose

  

The Notification Domain is responsible for delivering business communications generated by the platform.

  

Unlike the Integration Domain, which establishes connectivity with external systems, the Notification Domain manages the complete lifecycle of outbound messages. It determines **what should be communicated**, **when it should be delivered**, **which delivery channels should be used**, and **how failures should be handled**.

  

Notifications are treated as asynchronous operational workflows rather than synchronous business logic. This ensures that message delivery never blocks or compromises the execution of reservation transactions.

  

---

  

## Responsibilities

  

The Notification Domain is responsible for:

  

- Notification orchestration

- Delivery scheduling

- Message template management

- Multi-channel delivery

- Retry management

- Failure handling

- Notification preferences

- Delivery history

- Notification status tracking

- Localization

- Message rendering

- Event subscription

  

Typical entities include:

  

- Notification

- Notification Template

- Notification Event

- Delivery Attempt

- Delivery Channel

- Notification Preference

- Notification Queue

  

Rather than generating notifications directly, the Notification Domain subscribes to business events emitted by other domains.

  

---

  

## Dependencies

  

The Notification Domain depends on:

  

- Core Domain

- Authentication Domain

- Tenant Domain

- Integration Domain

  

Business domains publish events.

  

The Notification Domain consumes those events.

  

Provider communication is delegated to the Integration Domain.

  

This dependency structure prevents notification logic from leaking into business workflows.

  

---

  

## Scaling Strategy

  

Notification workloads are naturally asynchronous and burst-oriented.

  

The Notification Domain is designed to scale independently through:

  

- Apache Kafka

- Message queues

- Retry workers

- Scheduled workers

- Dead-letter queues

- Horizontal worker scaling

- Rate limiting

- Batch processing

  

Future deployments may execute notification workers independently from the reservation platform without modifying business logic.

  

---

  

## Security

  

The Notification Domain must protect customer communication channels while preventing abuse.

  

Security responsibilities include:

  

- Message authorization

- Tenant isolation

- Recipient validation

- Notification preference enforcement

- Delivery authentication

- Provider credential isolation

- Abuse prevention

- Spam protection

  

Sensitive provider credentials remain owned by the Integration Domain and are never exposed to business modules.

  

---

  

## Policies

  

The Notification Domain owns all communication policies, including:

  

- Retry policies

- Delivery priorities

- Notification scheduling

- Quiet hours

- Rate limiting

- Localization

- Channel selection

- Template selection

- Deduplication

- Escalation policies

- Reminder intervals

  

These policies determine how notifications are delivered without requiring changes to business logic.

  

---

  

## Ownership

  

The Notification Domain exclusively owns:

  

- Notification lifecycle

- Notification templates

- Delivery history

- Delivery queues

- Notification preferences

- Delivery scheduling

- Message rendering

- Communication orchestration

  

The domain does **not** own business events.

  

Business events belong to the Core Domain.

  

The Notification Domain owns the communication process triggered by those events.

  

---

  

# Audit Domain

  

## Purpose

  

The Audit Domain provides immutable operational history for the platform.

  

Its purpose is not analytics but accountability.

  

Every significant business action should produce an immutable audit record.

  

---

  

## Responsibilities

  

Responsibilities include:

  

- Audit logging

- Actor tracking

- Business event history

- Compliance records

- Security auditing

- Operational traceability

- Investigation support

  

Typical entities include:

  

- Audit event

- Actor

- Action

- Resource

- Correlation ID

- Request metadata

  

---

  

## Dependencies

  

Every domain may publish audit events.

  

The Audit Domain should never modify business data.

  

It operates independently from transactional workflows.

  

---

  

## Scaling Strategy

  

Audit workloads are append-only.

  

Future improvements include:

  

- Kafka event ingestion

- Immutable storage

- Log archival

- Cold storage

- Partitioned tables

- Long-term retention

  

---

  

## Security

  

Security principles include:

  

- Append-only records

- Tamper resistance

- Immutable history

- Access control

- Encryption at rest

- Long-term retention

  

Audit records should never be updated or deleted through normal application workflows.

  

---

  

## Policies

  

Policies include:

  

- Event retention

- Data retention

- Archival strategy

- Compliance requirements

- Log integrity

- Correlation standards

  

---

  

## Ownership

  

The Audit Domain exclusively owns:

  

- Audit events

- Operational history

- Actor history

- Security logs

- Business event records

  

It does **not** own business entities; it owns the historical evidence that those entities were created, modified, or acted upon.

  

---

  

# Dependency Principles

  

- **Authentication** establishes identity but has no knowledge of reservation logic.

- **Tenant** establishes organizational ownership and isolation.

- **Core** contains all reservation business rules and remains the system's source of truth.

- **Integration** reacts to business events and communicates with external systems without embedding external concerns into the Core domain.

- **Audit** records immutable historical events from all domains without participating in business decisions.