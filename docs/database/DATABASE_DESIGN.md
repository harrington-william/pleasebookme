# Domains List

- core
- auth
- audit
- resource
- notification
- webhook
- tenant
- widget
- organization
- customer
- analytics
- billing

---

Rule:
- Every id primary key columns are BIGSERIAL auto increment. Some special tables like core.booking has uid as a dedicated unique identity because they may used by external tools.

---

# Domain Ownership

- **Core** owns reservation orchestration.
- **Auth** owns identity and authorization.
- **Resource** owns allocatable assets and their operational state.
- **Tenant** owns SaaS account configuration and isolation.
- **Widget** owns distribution and embedding.
- **Audit** owns immutable historical evidence.
- **Organization** owns business hierarchy.
- **Integration** owns external provider connectivity.

---

# Domain Architecture

The platform is organized into multiple architectural domains that
separate responsibilities according to business capabilities rather than
technical implementation details. Each domain owns its own business
logic, persistence model, security policies, and operational
responsibilities.

Although all domains currently execute within a centralized Spring Boot
application and share the same PostgreSQL instance, they are
intentionally designed as independent modules with clear ownership
boundaries. This architectural separation minimizes coupling, improves
maintainability, and prepares the platform for future service
decomposition without requiring significant domain redesign.

## 13 Domains

- Core
- Auth
- Tenant
- Resource
- Organization
- Customer
- Widget
- Integration
- Notification
- Audit
- Webhook
- Analytics
- Billing

---

# Domain Dependency Rules

The platform follows strict ownership boundaries.

- Core owns reservation workflows.
- Resource owns reservable assets.
- Customer owns customer relationships and CRM.
- Auth owns identity and authentication.
- Tenant owns tenant isolation and platform ownership.
- Widget owns widget registration and distribution.
- Integration owns third-party connectivity.
- Notification owns communication orchestration.
- Audit owns immutable operational history.
- Webhook owns outbound event publication.
- Organization owns business hierarchy and specialized booking PBAC.
- Analytics owns reporting and business intelligence.
- Billing owns subscriptions and monetization.

No domain may directly modify another domain's aggregate outside of its
published application interface.

---

# Core Domain

## Purpose

The Core Domain is the reservation engine of the platform. It owns
booking workflows, scheduling, availability computation, booking
policies, attendee management, and reservation lifecycle management.

It never owns reservable assets, customer relationships, or
authentication.

## Responsibilities

- Reservation lifecycle
- Booking policies
- Availability computation
- Schedule management
- Booking validation
- Conflict detection
- Attendee management
- Reservation state transitions
- Business service configuration

## Typical Entities

- Service
- Booking
- Booking Policy
- Schedule
- Availability
- Selected Slot
- Attendee
- Out Of Office

## Dependencies

- Auth
- Tenant
- Resource
- Integration
- Audit

## Ownership

The Core Domain exclusively owns reservation workflows.

Resources belong to the Resource Domain.

Customers belong to the Customer Domain.

---

# Resource Domain

## Purpose

The Resource Domain manages every reservable asset that can participate
in a reservation.

## Responsibilities

- Resource lifecycle
- Resource categorization
- Resource pricing
- Resource assignment
- Resource schedules
- Capacity management
- Maintenance windows
- Availability overrides

## Typical Entities

- Resource
- Resource Type
- Resource Pricing
- Resource Assignment
- Resource Calendar
- Resource Maintenance
- Resource Attribute

## Dependencies

- Auth
- Tenant
- Core

## Ownership

The Resource Domain owns reservable assets but never reservation
workflows.

---

# Customer Domain

## Purpose

The Customer Domain manages the long-term relationship between tenants
and their customers.

Customers represent the customers of a tenant, never the tenant itself.

## Responsibilities

- Customer profile management
- Customer activities
- Customer notes
- Customer tags
- Marketing sources
- CRM foundation

## Typical Entities

- Customer
- Customer Activity
- Customer Note
- Customer Tag
- Customer Source

## Dependencies

- Tenant
- Core

## Ownership

The Customer Domain owns customer relationships, not reservations.

---

# Auth Domain

## Purpose

The Auth Domain manages identity, authentication and authorization.

## Responsibilities

- Users
- Roles
- Permissions
- Sessions
- OAuth Accounts
- Refresh Tokens
- API Keys

## Ownership

Only this domain stores credentials. Business identity — organizations,
staff profiles, memberships — belongs to the Organization Domain, not Auth.

---

# Organization Domain

## Purpose

The Organization Domain manages business identity and organizational
structure: the businesses themselves, staff profiles, membership within
them, and membership roles. It was split out of Auth to separate system
identity (credentials, sessions, RBAC) from business identity.

## Responsibilities

- Organization lifecycle
- Staff profiles
- Membership management
- Membership roles
- Business hierarchy and specialized booking PBAC

## Typical Entities

- Organization
- Profile
- Membership
- Membership Role

## Dependencies

- Auth

## Ownership

The Organization Domain owns business identity and staff membership. It
never stores credentials — that remains exclusively owned by Auth.
`tenant.tenants.organization_id` depends on this domain, so Organization
initializes before Tenant.

---

# Tenant Domain

## Purpose

Provides logical isolation between businesses sharing the same
infrastructure.

## Responsibilities

- Tenant lifecycle
- Plans
- Ecosystems
- Domains
- Quotas
- Feature entitlements

---

# Widget Domain

## Purpose

Manages embedded booking widgets distributed to tenant websites.

## Responsibilities

- Widget registration
- Origin validation
- Credential management
- Widget lifecycle
- Distribution

## Typical Entities

- Widget
- Widget Origin

## Ownership

The Widget Domain owns widget identity, never booking logic.

---

# Integration Domain

## Purpose

Provides connectivity with third-party systems.

## Responsibilities

- OAuth
- Google Calendar
- External APIs
- Provider abstraction

---

# Notification Domain

## Purpose

Owns outbound communication workflows.

## Responsibilities

- Notifications
- Templates
- Preferences
- Delivery history
- Queues
- Channels

---

# Audit Domain

## Purpose

Provides immutable operational history across the platform.

## Responsibilities

- Audit events
- Actors
- Resources
- Changes
- Correlation tracking

---

# Webhook Domain

## Purpose

Publishes platform events to external systems.

## Responsibilities

- Endpoint registration
- Signing
- Retry
- Delivery history

---

# Analytics Domain

## Purpose

Transforms operational data into business intelligence.

## Responsibilities

- Reporting
- Customer insights
- Resource utilization
- Operational dashboards

---

# Billing Domain

## Purpose

Owns subscriptions and platform monetization.

## Responsibilities

- Plans
- Invoices
- Payments
- Usage tracking
- Quotas

---

# Domain Dependency Diagram

```
                Auth
                  │
                  │
              Organization
                  │
                Tenant
                  │
      ┌───────────┴───────────┐
      │                       │
   Resource                Customer
      │                       │
      └───────────┬───────────┘
                  │
                Core
        ┌─────────┼─────────┐
        │         │         │
    Widget   Notification Integration
        │         │         │
        └─────────┼─────────┘
                  │
                Audit
```
