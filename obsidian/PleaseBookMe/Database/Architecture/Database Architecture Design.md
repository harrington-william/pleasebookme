# Database Architecture

## 1. Overview

Please Book Me's database is a single PostgreSQL instance that serves as the system of record for a multi-tenant booking infrastructure platform. It backs one Spring Boot modular monolith (`server/`) during the current PLATFORM V1.0.0 phase, with Redis used alongside it for short-lived, non-authoritative state.

The database is organized into 13 Postgres schemas that mirror the application's bounded contexts one-to-one (`auth`, `organization`, `tenant`, `core`, `resource`, `customer`, `widget`, `integration`, `notification`, `audit`, plus the not-yet-implemented `webhook`, `billing`, `analytics`), and a `public` schema holding a small set of cross-domain enum types. This schema-per-domain layout is the primary architectural device the system uses to keep domain boundaries legible inside a single shared database, ahead of any future move to physically separate databases or services.

A defining trait of this database, distinct from a conventional multi-tenant SaaS schema, is that **tenant scoping is not uniform**. `organization.organizations` is the root business-identity boundary that every domain ultimately hangs off; `tenant.tenants` is a *subscription overlay* on top of an organization, created only once an organization actually subscribes to a plan. Consequently some domains (`customer`, `widget`, `auth.api_keys`) key their isolation boundary directly off `tenant_id`, while the reservation engine itself (`core`, `resource`) keys off `organization_id` and has no `tenant_id` column at all. Section 6 documents this distinction per schema, and section 11 documents why it exists.

## 2. Architectural Model

- **Architecture:** `HYBRID` — modular monolith application, shared relational database, schema-based domain ownership, with a Redis-backed ephemeral layer for security-critical short-lived state.
- **Database Model:** Shared (`SHARED_DATABASE`) — one PostgreSQL instance, one connection pool, domain isolation enforced by schema boundaries and application-layer conventions rather than physical database separation.
- **Primary Database:** PostgreSQL 16.
- **System of Record:** PostgreSQL. Redis holds only derived, expiring, single-use state — never the durable record of an entity.
- **Consistency Model:** Strong/transactional within a single Postgres transaction (the unit the JPA/Hibernate + `@Transactional` layer operates on); no distributed transactions or cross-service sagas exist because there are no separate services yet. Redis-backed state (OAuth flow state, session handoff codes) is consumed exactly once via atomic `GETDEL`, which is a concurrency guarantee, not a consistency model shared with Postgres.

This is a modular monolith deliberately architected as if it were a set of services that happen to share infrastructure. Domain-driven package boundaries in the application (`auth/`, `organization/`, `tenant/`, `core/`, `resource/`, `customer/`, `widget/`, `integration/`, `notification/`, `audit/`) map 1:1 onto Postgres schemas, and the platform's own long-term roadmap (`docs/database/DATABASE_DESIGN.md`, `AGENTS.md`) states this is intentional preparation for future service decomposition, not incidental structure. The practical implication for anyone changing the schema: a table's schema assignment is a domain-ownership statement, not just a namespacing convenience.

## 3. Database Technology

### Primary Database

PostgreSQL 16, run via `infrastructure/docker/compose.yml` locally (Spring Boot's docker-compose integration starts/stops it automatically). Schema is owned entirely by Flyway (`flyway-database-postgresql`); Hibernate's `ddl-auto` is never used to generate or alter schema — entities are written to match migrations, never the reverse.

Extensions enabled at `V1__create_extensions.sql`: `pgcrypto` (backs `gen_random_uuid()` used by every `uid` column default), `citext`, `uuid-ossp`.

### Supporting Data Stores

**Redis 7.4** (`spring-boot-starter-data-redis`, Lettuce client), password-protected, bound to localhost, `--appendonly yes`, `--maxmemory-policy noeviction`.

| Concern | Purpose | Ownership | Durability | Authoritative? |
|---|---|---|---|---|
| OAuth state (`oauth:state:<token>`) | Binds a Google consent callback back to the initiating request (CSRF/replay defence, PKCE verifier storage) | `integration/oauthstate/` | 10-minute TTL, `GETDEL` single-use | No — derived from an in-flight request, never a record of anything persisted |
| Session handoff (`oauth:handoff:<code>`) | Crosses the origin boundary between the backend OAuth callback and the frontend, without putting a JWT in a URL | `auth/handoff/` | 60-second TTL, `GETDEL` single-use | No — a lookup key to a user reference, never a token |

Both stores are deliberately schema-less: neither has an entity, a repository, or a migration. The architectural reasoning (documented in `SERVER_AGENTS.md`/`SECURITY.md`) is that a Postgres table would need a cleanup job for expiry that Redis TTL gives for free, and that `GETDEL` gives single-use consumption in one atomic command that a table-plus-DELETE pair cannot guarantee under concurrent access. `--maxmemory-policy noeviction` is a deliberate choice over the Redis default `allkeys-lru`: silently evicting an OAuth state entry under memory pressure would turn a valid consent callback into an "invalid state" error for a real user, so the system fails loudly (a rejected `SET ... NX`) instead of silently dropping security state.

Redis is explicitly not the system of record for anything — every value it holds either expires unread or is consumed exactly once and converted into a Postgres write (or nothing at all, on abandonment).

## 4. Logical Organization

The database is organized around **schema-per-bounded-context**, where "bounded context" follows the same domain-driven boundaries the application package structure uses (see `SERVER_AGENTS.md`'s "Package structure"). Every table belongs to exactly one schema, and that schema is the table's ownership statement.

Two schemas fall outside the domain-per-schema pattern:

- **`public`** holds only cross-domain enum types shared by multiple schemas' tables — `locale`, `theme`, `week_start`, `currency` — never any table. A `public`-schema enum exists because more than one domain's columns use the same value set (e.g. both `auth.users.locale` and `core.services.interface_language` use `public.locale`); an enum scoped to a single schema (e.g. `core.booking_status`) stays there instead.
- **`common`**-prefixed migrations (`V1`–`V9`) are infrastructure-setup migrations (extensions, schema creation, shared enum creation) that run before any domain's own migrations, not a schema of their own.

### Schema Overview

| Schema | Owner (domain) | Responsibility | Scope |
|---|---|---|---|
| `auth` | Auth | Identity, credentials, RBAC (roles/permissions), sessions, API keys | Platform-wide; per-user |
| `organization` | Organization | Business identity — organizations, staff profiles, membership, membership roles | Organization |
| `tenant` | Tenant | Subscription/plan overlay, ecosystems, custom domains, quota configuration | Tenant (1:1 with a subscribing organization) |
| `core` | Core | Reservation engine — services, schedules, availability, booking policies, bookings, attendees, out-of-office | Organization → Service |
| `resource` | Resource | Bookable/allocatable assets — resource types, resources, pricing, assignment, maintenance, overrides | Organization → Service |
| `customer` | Customer | Tenant's CRM — customer profiles, notes, activity, tags, sources | Tenant → Organization |
| `widget` | Widget | Embedded booking widget identity, credentials, origin validation | Tenant |
| `integration` | Integration | Delegated third-party authorization (Google), destination bindings, sync job queue | User (credential) → Service (destination) |
| `notification` | Notification | Outbound communication — templates, channels, delivery, queueing, preferences | Tenant (nullable) → User |
| `audit` | Audit | Immutable historical evidence of platform activity | Tenant (nullable) / platform-wide |
| `webhook` | Webhook | Outbound event publication to external systems | *Not yet implemented — schema created, no tables* |
| `billing` | Billing | Subscriptions, invoicing, usage-based monetization | *Not yet implemented — schema created, no tables* |
| `analytics` | Analytics | Reporting and business intelligence | *Not yet implemented — schema created, no tables* |
| `public` | Shared/platform | Cross-domain enum types only (`locale`, `theme`, `week_start`, `currency`) | Global |

`webhook`, `billing`, and `analytics` are deliberately excluded from PLATFORM V1.0.0 scope (`AGENTS.md`: "these are premium feature, not prerequisites of the system... just increase complexity"). Their schemas exist in `V2__create_schemas.sql` so a future migration can add tables to them without a schema-creation migration, but as of the current applied version (v125) they contain no tables.

## 5. Data Ownership

Ownership in this database is expressed structurally, at the schema boundary, and reinforced by an application-layer convention: each schema has exactly one Spring Boot package tree responsible for writing to it (e.g. `resource/` for `resource.*`), and no other package writes to another domain's tables directly — all cross-domain interaction goes through that domain's service layer or, for read-only FK resolution, its repository.

Ownership is distinguished from access and reference throughout this document:

- **Data ownership** — the schema/domain whose service layer is the sole writer of a table (e.g. `core` owns `core.bookings`; only `core`'s `BookingService` mutates it).
- **Data access** — another domain reading owned data through the owner's repository/service to make a decision (e.g. `WidgetIdentityLoader` reads `widget.widgets` and `widget.widget_origins`, both of which it owns for that purpose, to admit a widget actor).
- **Data reference** — a foreign key from one domain's table into another's, which grants no write permission (e.g. `core.services.organization_id` references `organization.organizations`, but `core` never writes to `organization.organizations`).

`tenant.tenants` is the one deliberate exception worth naming explicitly: it is written by the `service/workspace/` orchestration layer (`WorkspaceProvisioningService`) rather than by `tenant`'s own CRUD service exclusively, because tenant creation is a side effect of a cross-domain workflow (user registration provisioning a workspace), not a standalone tenant-management operation. This is documented further in `SERVER_AGENTS.md`'s "Workspace provisioning" section and is a known, tracked gap rather than settled architecture — see section 17.

## 6. Domain / Schema Boundaries

### `auth`

- **Owner:** Auth domain
- **Responsibility:** Platform identity and credentials — who can authenticate, and with what authority once authenticated.
- **Owns:** `users`, `user_passwords`, `roles`, `permissions`, `user_roles`, `role_permissions`, `accounts` (OAuth provider linkage — identity only, not tokens), `refresh_tokens`, `api_keys`.
- **May reference:** `organization.organizations` (a raw FK reference, not ownership — e.g. none in this schema currently), `tenant.tenants` (`api_keys.tenant_id`).
- **Must not modify:** Business identity (`organization.*`) or subscription state (`tenant.*`). `auth` answers "who is this and what may they do," never "what business do they run."

`auth.accounts` originally stored Google OAuth `access_token`/`refresh_token`/`id_token` alongside the provider link (`V16`), but `V113__auth_accounts_drop_oauth_tokens.sql` removed all five token columns. This is a load-bearing architectural fact, not an incidental cleanup: it marks the point where `auth.accounts` became purely an *identity-linking* table (which platform user corresponds to which Google `sub`) and `integration.oauth_connections` became the sole store of live Google credentials. Sign-in (authentication, "who is this?") and delegated authorization (integration, "may we act on their behalf?") are architecturally distinct per `SECURITY.md` §11–12, and this migration is the schema enforcing that separation — a Sign-In credential link and a Calendar/Sheets/Drive grant no longer share storage.

`auth.refresh_tokens` similarly evolved away from a `widget_id` FK (`V63` added it, `V99__drop_refresh_token_widget.sql` removed it) toward a generic `owner` enum (`USER` / `WIDGET` / `TENANT`, added `V96`–`V97`). A refresh token's owner is now expressed as a discriminator value rather than a nullable FK to one specific owning table, which is the schema anticipating more than two owner kinds without a new FK column per kind.

### `organization`

- **Owner:** Organization domain
- **Responsibility:** Business identity — the businesses themselves, the staff who work in them, and their membership/role structure. Split out of `auth` specifically to separate system identity (credentials, RBAC) from business identity.
- **Owns:** `organizations`, `profiles`, `memberships`, `membership_roles`.
- **May reference:** `auth.users` (a profile/membership belongs to a user).
- **Must not modify:** Credentials (`auth.*`) or subscription/quota state (`tenant.*`).

`organization.organizations` is the true root of the business hierarchy — every tenant, service, resource, customer, and widget ultimately traces back to an organization, and an organization requires no active subscription to exist (see section 11). `tenant.tenants.organization_id` depends on `organization.organizations`, so `organization`'s migrations (`V18`–`V21`) apply before `tenant`'s (`V22`–`V25`).

### `tenant`

- **Owner:** Tenant domain
- **Responsibility:** The subscription/plan layer over an organization — plan tier, ecosystem classification, region, quotas, and custom domains. Provides the isolation unit that `customer`, `widget`, and `auth.api_keys` scope directly against.
- **Owns:** `tenants`, `plans`, `ecosystems`, `tenant_domains`.
- **May reference:** `organization.organizations`, `auth.users` (owner), and (once implemented) resolves `ecosystem_id`/`plan_id` against its own lookup tables.
- **Must not modify:** The organization it belongs to, or any operational data (`core.*`, `resource.*`) — a tenant configures quotas and plan entitlements, it does not orchestrate reservations.

A `tenant.tenants` row is not guaranteed to exist for every organization. It represents an active subscription and is created only when an organization actually subscribes to a plan — this is the mechanism that lets the platform support a free, no-plan-required entry point. See section 11 for the full implication chain.

### `core`

- **Owner:** Core domain
- **Responsibility:** The reservation engine — booking lifecycle, scheduling, availability computation, booking policy configuration, attendee management. This is the platform's original domain (MVP v1) and the reason the product exists.
- **Owns:** `schedules`, `availabilities`, `services`, `booking_policies`, `bookings`, `attendees`, `selected_slots`, `out_of_office`.
- **May reference:** `auth.users`, `organization.profiles`, `organization.organizations`, `integration.destination_calendars`/`destination_sheets` (nullable — a service/booking may sync to an external calendar/sheet, but does not require one).
- **Must not modify:** Reservable assets (`resource.*`), customer relationships (`customer.*`), or tenant configuration (`tenant.*`). Core computes availability and manages booking state; it does not own what is being booked or who the customer relationship belongs to.

`core.services` and `core.bookings` are scoped by `organization_id` and `user_id`/`profile_id`, never by `tenant_id` directly — there is no `tenant_id` column anywhere in this schema. This is the clearest structural evidence of the organization-vs-tenant distinction in section 11: an organization with no tenant subscription can still fully operate the booking engine.

### `resource`

- **Owner:** Resource domain
- **Responsibility:** Every allocatable asset that can participate in a reservation — the thing being booked, distinct from the booking itself.
- **Owns:** `resource_types`, `resources`, `resource_pricing`, `resource_assignments`, `resource_calendars`, `resource_maintenance`, `resource_attributes`, `resource_overrides`.
- **May reference:** `organization.organizations`, `core.services`, `core.schedules`, `organization.memberships` (assignment target).
- **Must not modify:** Reservation state (`core.bookings`) — a resource can be assigned, maintained, or overridden, but the booking lifecycle itself belongs to `core`.

Like `core`, this schema is scoped by `organization_id`, not `tenant_id` — the same organization-first isolation pattern applies.

### `customer`

- **Owner:** Customer domain
- **Responsibility:** The long-term relationship between a tenant and the people who book with it — CRM data, distinct from the reservation records themselves.
- **Owns:** `customers`, `customer_notes`, `customer_activities`, `customer_tags`, `customer_sources`.
- **May reference:** `tenant.tenants`, `organization.organizations`, `auth.users` (note author).
- **Must not modify:** Reservation records (`core.bookings`) or attendee data captured at booking time (`core.attendees`) — a `customer.customers` row is a CRM profile the tenant maintains; `core.attendees` is a per-booking snapshot. The two are deliberately separate concepts, not a normalization the schema collapsed.

`customer.customers` is the one schema (besides `auth.api_keys` and `widget.widgets`) scoped directly by `tenant_id`, with `organization_id` also present as a secondary FK. Both composite unique constraints (`(tenant_id, email)`, `(tenant_id, phone)`) are keyed on tenant, not organization — the CRM identity boundary is explicitly the paying tenant, not the underlying organization.

### `widget`

- **Owner:** Widget domain
- **Responsibility:** Identity and distribution of the embedded booking widget — the first client of the platform, not the platform itself.
- **Owns:** `widgets`, `widget_origins`.
- **May reference:** `tenant.tenants`.
- **Must not modify:** Booking logic (`core.*`) — a widget is a credentialed, origin-validated caller of the booking API, not a participant in reservation orchestration.

`widget.widgets.service_id` was removed (`V98__drop_widgets_service_column.sql`) — a widget is now scoped only to a tenant, not bound to one specific service at the schema level, reflecting that a single embedded widget can present multiple services from the same tenant.

### `integration`

- **Owner:** Integration domain
- **Responsibility:** Delegated third-party authorization and connectivity — Google Calendar, Sheets, and Drive. Answers "may we act on this user's behalf," never "who is this user."
- **Owns:** `oauth_connections`, `destination_calendars`, `destination_sheets`, `destination_drives`, `sync_jobs`. Also owns the Redis-backed `oauthstate` concern (section 3), which has no table.
- **May reference:** `auth.users` (the user who granted consent), `core.services`/`core.bookings` (the destination a sync targets).
- **Must not modify:** The identity pipeline (`auth.*`) — a Google consent grant never mints a session by itself; it is joined to identity only via the shared `GoogleAccountResolver` (see `SECURITY.md` §11, §14).

`integration.oauth_connections` (added `V115`) centralizes every Google credential the platform holds; `destination_calendars`/`destination_sheets` were retrofitted (`V118`, made `NOT NULL` in `V125`) to FK into it rather than holding independent state, and `destination_drives` (`V117`) was built against it from the start. `integration.sync_jobs` (`V120`) is a durable outbox/retry queue for calendar/sheet/drive synchronization, keyed to both a `core.bookings` row and an `oauth_connections` row — the first table in the database representing asynchronous, retryable work rather than a direct-write operational record.

### `notification`

- **Owner:** Notification domain
- **Responsibility:** Outbound communication orchestration — templates, delivery, queueing, and per-user preferences.
- **Owns:** `notification_channels`, `notification_templates`, `notifications`, `notification_preferences`, `notification_queue`, `notification_deliveries`.
- **May reference:** `tenant.tenants` (nullable — a template/notification may be platform-global), `organization.organizations`, `auth.users`.
- **Must not modify:** The business event that triggered a notification (`core.*`, `auth.*`, etc.) — notification is a downstream consumer of platform events, not a producer of the operational state it describes.

`notification_templates.tenant_id` and `notifications.tenant_id` are both `NOT NULL`-with-nullable exceptions handled per table (`notification_templates.tenant_id` is nullable to allow platform-default templates); this is the domain's own overlay of the tenant-optionality pattern rather than a strict requirement.

### `audit`

- **Owner:** Audit domain
- **Responsibility:** Immutable historical evidence of platform activity, for security and operational forensics — explicitly not the operational source of truth for the events it records.
- **Owns:** `audit_actors`, `audit_events`, `audit_resources`, `audit_changes`.
- **May reference:** `tenant.tenants`, `organization.organizations` (both nullable, `ON DELETE SET NULL`), `auth.users` (via `audit_actors`, deliberately without a real FK — see below).
- **Must not modify:** Anything outside its own four tables. No other domain's write path is gated on an audit write succeeding.

`audit_actors`' references to `auth.users`/`organization.memberships`/`widget.widgets`/`auth.api_keys` are **deliberately not real foreign keys** — confirmed in code comments and `SERVER_AGENTS.md`: "Audit exists to be immutable historical evidence. A real FK with CASCADE would delete audit history when the referenced row is deleted, which defeats the point." This is the one place in the schema where referential integrity is intentionally *not* enforced at the database level, in service of a stronger architectural guarantee (audit history outlives the rows it describes).

### `webhook`, `billing`, `analytics`

- **Owner:** Their respective future domains.
- **Responsibility:** Documented in `docs/database/DATABASE_DESIGN.md` (outbound event publication; subscriptions/monetization; reporting/BI, respectively).
- **Owns:** Nothing yet — schemas exist, no tables.
- **Status:** Explicitly out of scope for PLATFORM V1.0.0 per `AGENTS.md`. Do not add tables here without confirming the phase has actually changed.

## 7. Data Relationships and Coupling

### Direct Relationships

The overwhelming majority of cross-schema coupling is a standard Postgres foreign key with a named constraint (per `CODING_CONVENTIONS.md`, inline FK constraints are prohibited — every FK is a separate `CONSTRAINT fk_... FOREIGN KEY ... REFERENCES ...`). The dependency graph, derived from actual FK targets rather than narrative:

```
auth.users
    │
    ├──► organization.profiles / memberships   (user_id)
    │
organization.organizations
    │
    ├──► tenant.tenants            (organization_id)
    ├──► core.services             (organization_id)
    ├──► resource.resources        (organization_id, via resource_types)
    ├──► customer.customers        (organization_id)
    ├──► audit.audit_events        (organization_id, nullable)
    │
tenant.tenants
    │
    ├──► customer.customers        (tenant_id)
    ├──► widget.widgets            (tenant_id)
    ├──► auth.api_keys             (tenant_id)
    ├──► notification.notifications (tenant_id)
    ├──► audit.audit_events        (tenant_id, nullable)
    │
core.services
    │
    ├──► resource.resources        (service_id)
    ├──► core.booking_policies     (service_id)
    ├──► integration.destination_calendars / destination_sheets / destination_drives (service_id)
    │
core.bookings
    │
    ├──► core.attendees            (booking_id)
    ├──► integration.sync_jobs     (booking_id)
    │
integration.oauth_connections
    │
    └──► integration.destination_calendars / destination_sheets / destination_drives / sync_jobs (oauth_connection_id)
```

`ON DELETE CASCADE` is the default across almost every FK in the schema, meaning deleting a parent row (an organization, a tenant, a service) removes its dependent rows transitively rather than orphaning them. `ON DELETE SET NULL` is reserved for FKs where the relationship is informational rather than defining (e.g. `core.bookings.cancelled_by`, `widget.widget_origins.created_by`, `audit.audit_events.tenant_id`) — losing the referenced row shouldn't destroy the row that merely references it.

### Indirect Relationships

- **Redis-mediated handoff** (`integration/oauthstate/`, `auth/handoff/`) — an application-layer indirection, not a database relationship at all. A Postgres write (the eventual `oauth_connections` upsert, or a provisioned user) is deferred until after a Redis-stored token is consumed, decoupling the browser-facing OAuth round trip from the transactional Postgres write.
- **Encrypted-at-rest coupling** — `integration.oauth_connections.access_token`/`refresh_token` are ciphertext (AES-256-GCM, `security/crypto/`), with `token_key_version` recording which key encrypted them. This is a coupling between the database row and an out-of-database key configuration (`security.token-encryption.*`), not a schema relationship, but it is architecturally significant: the column's meaning cannot be understood from the schema alone.
- **Application-orchestrated cross-domain writes** — `WorkspaceProvisioningService` writes to `auth`, `organization`, and `tenant` and `core` (and, per the planned scope in `SERVER_AGENTS.md`, `notification`) inside one transaction, triggered by a single registration event. This is the one place several domains' tables are written together atomically by design, not by accident — see section 8.

### Coupling Rules

- A schema may hold a foreign key into another schema's table (a **reference**), but the referencing schema's own service/repository layer never issues a write against the referenced table.
- `audit.*`'s FKs into `auth`/`organization`/`widget`/`auth.api_keys` are the sole intentional exception to "always use a real FK" — replaced with plain unconstrained columns specifically to prevent cascading deletes from erasing history (section 6).
- Redis-held state must never become the only record of a fact that matters after the flow that created it completes; every durable outcome (a provisioned user, a persisted OAuth connection) lands in Postgres before the Redis key would otherwise be relied upon again.

## 8. Transaction and Consistency Model

### Transaction Boundaries

Every domain's standard `create`/`update` service method runs inside Spring's implicit transaction from `SimpleJpaRepository.save()` — single-row writes need no explicit `@Transactional`. Multi-row, cross-domain writes get an explicit transaction boundary on a dedicated orchestration bean:

- **Registration** (`AuthServiceImpl.register()`) — writes `auth.users`, `auth.user_passwords`, `auth.user_roles`, `organization.organizations`, `organization.memberships`, `organization.profiles` atomically, then immediately issues a token pair. A mid-way failure must not strand a user with only some of these rows.
- **Google one-shot onboarding** (`GoogleOnboardingService.finalizeOnboarding`, `@Transactional`, its own bean specifically because Spring does not proxy self-invocation) — writes a user, role assignment, organization, membership, profile, `auth.accounts` link, and `integration.oauth_connections` row in one transaction, plus a Redis handoff-code write inside the same boundary (safe because Redis is local and sub-millisecond; if the commit fails afterward, the orphaned Redis key simply expires).

A deliberate, documented property of both flows: the transaction boundary starts **after** any third-party network call (Google's token endpoint), never around it. Holding a Postgres connection open across an external HTTP round trip is explicitly avoided.

### Consistency

Strong/transactional consistency is the default and, as of the current implementation, the only consistency model actually in use for Postgres writes — there is no asynchronous event bus, no eventual-consistency projection, and no cross-service saga, because there is one service. `integration.sync_jobs` is the one structure in the schema built for eventual, retried processing (`status`, `attempts`, `max_attempts`, `available_at`, `last_error`), but it is a durable work queue consumed by a worker within the same application, not a distributed consistency mechanism between separate systems.

### Idempotency

- `core.bookings.idempotency_key` exists as a plain nullable, unvalidated `VARCHAR` — the migration declares no uniqueness on it, so it is not currently enforced as a dedup mechanism at the database level (see `SERVER_AGENTS.md`'s note that inventing an unstated constraint is out of scope for this codebase's conventions).
- Redis `GETDEL` on `oauth:state:<token>` and `oauth:handoff:<code>` is the actual enforced idempotency/single-use mechanism in the system — a replayed state or handoff code finds nothing on its second use.
- `integration.oauth_connections`'s unique constraint on `(user_id, provider, provider_account_id)` prevents a duplicate connection row from two concurrent consent flows for the same Google account, though `SECURITY.md` §15 notes the loser of that race currently surfaces as a raw 500 rather than a handled retry.

## 9. Data Integrity Strategy

### Database-Level Integrity

- **Primary keys**: `BIGSERIAL` on every table (per `CODING_CONVENTIONS.md`), with `core.bookings`, and every entity exposing a public/external identifier (widgets, tenants, customers, resources, notifications, etc.), additionally carrying a `UUID NOT NULL DEFAULT gen_random_uuid()` surrogate `uid` for external-facing references.
- **Foreign keys**: every cross-table relationship except `audit.audit_actors`' four reference columns (section 6) is a named `CONSTRAINT fk_... FOREIGN KEY`, never an inline constraint.
- **Unique constraints**: named, table-scoped (e.g. `uq_tenants_slug`, `uq_customers_tenant_email`), including composite uniqueness where a business key spans multiple columns (`(organization_id, slug)` on `core.services`/`resource.resources`; `(tenant_id, email)`/`(tenant_id, phone)` on `customer.customers`).
- **Not-null constraints**: applied per-column to match the migration's declared requiredness — the entity layer mirrors this exactly rather than relaxing it.
- **Check constraints**: not used in this schema; validity rules that would otherwise be a `CHECK` (e.g. enum-like status strings on `resource.resources.status`, `core.services.period_type`) are instead modeled as either a native Postgres enum type or, in several tables, a plain `VARCHAR` left unconstrained at the DB level — see the "look-alike but actually `VARCHAR`" pattern documented extensively in `SERVER_AGENTS.md`. This is a real, current gap: several status-shaped columns accept any string at the database level and rely entirely on the application layer for valid-value enforcement.

### Application-Level Integrity

- Bean Validation (`jakarta.validation.constraints.*`) on every `Request` DTO, mapped field-by-field off the migration's own constraints (`@NotBlank`/`@Size` for required bounded strings, `@NotNull` for required non-string/FK columns) — this duplicates DB-level requiredness at the API boundary so a violation surfaces as a clean 400 rather than a raw constraint-violation 500.
- Duplicate-check-before-create: every domain's `create` method calls the repository's `existsBy*` for each unique constraint before building the entity, converting a would-be DB unique-violation into a typed `Duplicate<Name>Exception` (409) instead of a raw SQL error.
- FK existence validation: resolved via `findById().orElseThrow()` wherever the target domain's repository already exists; where it doesn't yet (a known, tracked gap — e.g. `resource.resource_assignments.membership_id` pending `MembershipRepository`), a bare reference entity is constructed instead, which skips existence validation and lets an invalid FK surface as a raw DB-level violation. This gap is itself documented per-occurrence in `SERVER_AGENTS.md` rather than silently accepted.

### Domain Integrity

- **Every `UserPrincipal` must resolve to a `Membership` and a `Profile`** (enforced via `orElseThrow` in `DefaultUserIdentityLoader`) — a user without either is treated as a data-integrity error, since `register()` always creates both atomically.
- **A `Tenant` is not required for a `UserPrincipal` to be valid** — resolved via `orElse(null)`, the one deliberate exception to the rule above, because tenant represents an active subscription rather than a prerequisite of having an account (section 11).
- **`integration.oauth_connections.refresh_token` is `NOT NULL` with no fallback** — a consent response with no refresh token on a *new* connection is skipped rather than written, specifically to avoid aborting a transaction that is also provisioning a user account (section 8).
- **Audit records must not gate other domains' writes** — no other domain's transaction depends on an audit write succeeding (section 6).

## 10. Data Access Rules

### Ownership Rules

Writes to a schema's tables are performed exclusively by that schema's own `service/impl/` layer (per `CODING_CONVENTIONS.md`'s interface/impl convention), with the two documented cross-domain exceptions in section 5 (`tenant.tenants` written by provisioning orchestration; `integration.oauth_connections` written exclusively by the consent flow, never via generic CRUD — see below).

### Read Rules

A domain reads another domain's data through that domain's repository (e.g. `BusinessServiceImpl` injects `OrganizationRepository`/`ProfileRepository`/`ScheduleRepository` to resolve FKs) — never via a raw query against another schema's table from outside its owning package.

### Write Rules

- Standard CRUD follows the `create`/`getById`/`getAll`/`update`/`delete` shape per `CODING_CONVENTIONS.md`, with a verb omitted only when the entity's actual shape doesn't support it (e.g. no `update` on a pure join table with nothing mutable) or when the domain's business policy forbids it (audit's `create`/read-only, section 12).
- **`integration.oauth_connections` is the one table in the schema where a verb was removed for security reasons, not shape reasons**: `POST`, `PUT`, and list-all were deleted from the generic `/api/v1/oauth-connections` CRUD surface, along with the request DTO that carried client-suppliable `accessToken`/`refreshToken`. Only an owner-scoped read and a delete remain; the OAuth consent flow is the sole sanctioned writer. This is documented in `SECURITY.md` §12 as "omit a verb whose existence is a security hole" — a third category alongside "shape doesn't support it" and "policy forbids it."

### Cross-Boundary Access

Cross-boundary access is mediated by shared orchestration components living outside any single domain's package (`service/auth/`, `service/integration/` per `SERVER_AGENTS.md`'s package structure) — e.g. `GoogleAccountResolver` is shared by both the Sign-In and one-shot-registration entry points specifically so account-resolution logic (including the security-critical `email_verified` gate) cannot drift between two independent implementations.

## 11. Security Architecture

### Isolation

Isolation in this database is **not a single uniform tenant_id filter** — it is layered, and the layer differs by schema (see section 6's per-schema notes and the worked example in section 1):

- **Organization-scoped domains** (`core`, `resource`): isolation boundary is `organization_id`. No `tenant_id` column exists in either schema. An organization with no tenant subscription still has full, isolated access to the booking engine.
- **Tenant-scoped domains** (`customer`, `widget`, `auth.api_keys`, `notification`): isolation boundary is `tenant_id` directly, because these represent paid-tier capabilities (CRM, embeddable widgets, programmatic API access, tenant-configured notification templates) gated behind an actual subscription.
- **Platform-wide domains** (`auth`, `audit`): no tenant/organization boundary at the table level for the core identity tables (`auth.users`, `auth.roles`), by design — identity is platform-scoped, authorization within an organization is layered on top via `organization.membership_roles`.

This asymmetry is the direct schema consequence of the product rule documented in `SERVER_AGENTS.md`/`SECURITY.md`: **every user is a tenant conceptually, but not every user has a `tenant.tenants` row** — tenant is a subscription, not an identity prerequisite. Code that makes a tenant-scoped decision off `UserPrincipal.tenantUid()` must null-check it; the schema itself enforces this by simply not having a `tenant_id` column on the tables that don't need a subscription to function.

### Authentication

Not a database-architecture concern per se — the full pipeline is documented in `SECURITY.md` §1–11. The database's role is limited to being the source `IdentityLoader` implementations query (`auth.users`, `organization.memberships`/`profiles`, `widget.widgets`) and the target `auth.refresh_tokens` persists issued sessions to.

### Authorization

RBAC is modeled relationally: `auth.roles` × `auth.permissions` via the join table `auth.role_permissions`, and `auth.users` × `auth.roles` via `auth.user_roles`. Six platform roles are seeded (`V89__seed_roles.sql`): `PLATFORM_OWNER`, `PLATFORM_MANAGER`, `USER`, `ORGANIZATION_OWNER`, `ORGANIZATION_MANAGER`, `STAFF`. Permissions follow a `<RESOURCE>.<ACTION>` slug convention (`auth.permissions.slug`, unique), seeded per-domain as plain CRUD verbs (`V79`–`V88`) with domain-specific actions (e.g. `BOOKING.REJECT`) explicitly deferred to a later seed pass rather than invented ahead of the service methods that would use them (`AGENTS.md`).

A second, narrower authorization concept exists for `organization.membership_roles`: role assignment scoped to one membership within one organization, distinct from the platform-wide `auth.user_roles` assignment — this is what `docs/database/DATABASE_DESIGN.md` calls "specialized booking PBAC" under the Organization domain.

`WidgetPrincipal` (backed by `widget.widgets`) deliberately has no roles/permissions relationship at all — a widget is a fixed-capability actor, not an RBAC participant (`SECURITY.md` §10).

### Sensitive Data

- **`integration.oauth_connections.access_token`/`refresh_token`**: AES-256-GCM ciphertext, fresh 12-byte IV per encryption, `token_key_version` recording which key version encrypted a given row so key rotation needs no re-encryption sweep. This is the only column-level encryption-at-rest in the schema.
- **`auth.api_keys.secret_hash`**: stored hashed, never the plaintext secret; `public_key` is stored and returned as-is since it is a public identifier, not a credential.
- **`auth.refresh_tokens.secret`**: stored as an opaque bearer credential, excluded from any API response DTO.
- **`auth.user_passwords`**: originally carried a plaintext `raw` column alongside `hash` (a deliberate, documented decision for the private-onboarding phase — see `db_raw_password_field` in project memory), but `V114__auth_user_passwords_drop_raw.sql` has since dropped that column. **This is a schema fact worth flagging explicitly: the plaintext-password-storage decision documented elsewhere in this project's memory and in `SERVER_AGENTS.md`'s entity conventions is now stale relative to the actual schema** — as of the current migration state, `auth.user_passwords` stores only `hash`, no plaintext. Treat any reference to a `raw` column in other documentation as describing a prior schema state, not the current one.

### Privileged Access

No row-level security policies, database roles beyond the application's own connection, or privilege-separated schemas are defined in the migrations inspected. Access control is enforced entirely at the application layer (Spring Security + the RBAC model above), not via Postgres `GRANT`/RLS. This is a `DOCUMENTARY`/structural observation, not a claim that such controls are unnecessary — see section 18's validation note.

## 12. Audit Architecture

### Audit Sources

`audit.audit_actors.actor_type` (native enum) enumerates the recognized actor categories: `GUEST`, `ATTENDEE`, `SYSTEM`, `WIDGET`, `API_KEY`, `WEBHOOK`, `INTEGRATION`. An audit actor is resolved independently of the live `auth.users`/`widget.widgets` rows it may correspond to (section 6) — it is a standalone identity snapshot, not a live join.

### Audit Scope

`audit.audit_events.event_domain` (native enum: `AUTH`, `CORE`, `RESOURCE`, `TENANT`, `WIDGET`, `INTEGRATION`, `NOTIFICATION`, `BILLING`, `SYSTEM`, `ORGANIZATION`) and `event_type`/`action`/`severity`/`status` classify what happened; `audit.audit_resources` captures the specific resource affected with `before_snapshot`/`after_snapshot` JSONB pairs (the one place in the schema where JSONB is the core payload rather than excluded optional metadata); `audit.audit_changes` captures individual field-level diffs against a resource.

### Immutability

Append-only by explicit business policy documented in `docs/architecture/LOW_LEVEL_ARCHITECTURE.md` and reinforced structurally: all four `audit.*` tables' service layers implement only `create`/`getById`/`getAll` — `update` and `delete` are deliberately never implemented, extending the "omit a verb the entity's shape doesn't support" convention to "omit a verb the domain's business policy forbids." This is a `BEHAVIORAL` claim (verifiable by inspecting the service classes) reinforced by, but not fully enforced at, the database level — there is no Postgres-level `REVOKE UPDATE`/RLS backing it (see section 11's Privileged Access note).

### Audit Boundary

Audit data is explicitly historical evidence, never the operational source of truth for the event it describes — a booking's current state lives in `core.bookings`, not reconstructed from `audit.audit_events`. The FK-less design of `audit.audit_actors`' reference columns (section 6) exists specifically to keep audit history outliving the operational rows it references.

## 13. Data Lifecycle

### Creation

Most tables follow standard CRUD creation via their owning domain's service. Two flows create rows across multiple schemas atomically (section 8): registration and Google one-shot onboarding.

### Modification

Full-replace `update` semantics are the standard (`CODING_CONVENTIONS.md`: the caller sends the complete resource, not a partial patch) — there is no PATCH-style partial update convention anywhere in the schema's application layer.

### Deletion

- **Physical deletion** is the default for domain entities exposing a `delete` verb, cascading via `ON DELETE CASCADE` through dependent rows (section 7).
- **Soft deletion** exists narrowly: `core.bookings.deleted_at`/`deleted_by` are plain nullable columns (not `@CreationTimestamp`/`@UpdateTimestamp`-managed), exposed like any other mutable field under full-replace CRUD rather than backed by a dedicated cancel/soft-delete workflow as of the current implementation.
- **No deletion**: `audit.*` (section 12), and `integration.oauth_connections`, where disconnect revokes the grant at Google and marks the row `REVOKED` rather than deleting it — the historical record of a past connection is retained.

### Archival

Not established. No table or migration implements an archival/cold-storage tier as of the current schema.

### Retention

Not established for any domain except the implicit "audit is retained indefinitely, being historical evidence" (section 12) — no explicit retention period or purge policy is defined anywhere in the migrations or documentation inspected.

## 14. Migration Strategy

### Migration Tool

Flyway (`flyway-database-postgresql`), owning the schema exclusively — Hibernate `ddl-auto` never generates or alters schema.

### Versioning

A single global, strictly incremental version sequence across every domain folder (`CODING_CONVENTIONS.md`) — the next migration is always the highest existing version anywhere in `db/migration` plus one, never a domain-local sequence. Sequencing follows real dependency order (a table-altering migration is always numbered after the migration that created the table), not authorship order. The current applied high-water mark is **v125** (per `SERVER_AGENTS.md`).

Migrations are authored twice, deliberately: first with explanatory comments in `database/init/<domain>/` (a separate, higher-numbered sequence, V10–V218 as observed, used for fresh-environment bootstrap/documentation), then mirrored comment-free into `server/src/main/resources/db/migration/<domain>/` for Flyway to actually execute — confirmed by comparing the two trees in this session: `server/`'s Flyway copy is presently ahead of `database/init/` (it has migrations through V125 covering `integration.oauth_connections`, `destination_drives`, `sync_jobs`, and several `auth`/`widget`/`tenant` schema evolutions that have not yet been mirrored back into `database/init/`). This gap is a known artifact of the "mirror on request" workflow, not a broken migration state — Flyway only ever executes from the `server/` copy.

### Deployment

Not established beyond local docker-compose (`infrastructure/docker/compose.yml`, auto-started by Spring Boot). No CI/CD pipeline or production migration runner is present in the inspected material — `AGENTS.md`/`CLAUDE.md` place "Automated CI/CD pipelines" and "Production environment management" as in-progress PLATFORM V1.0.0 objectives, not completed ones.

### Backward Compatibility

No explicit rolling-deployment compatibility policy is documented for schema changes. Renumbering a migration below the applied high-water mark is explicitly called out as a known failure mode (`SERVER_AGENTS.md`: a prior renumbering below the mark produced a Flyway `validate` failure surfacing as an opaque bean-creation error) — the operative rule is "never renumber below the current high-water mark," not a formal compatibility contract.

## 15. Reliability and Recovery

### Backup

`database/backup/` exists as a directory in the repository, but no backup strategy, schedule, or tooling is described in the inspected documentation. **Undetermined** beyond the directory's existence.

### Recovery

Not established. No RTO/RPO or recovery procedure is documented.

### Failure Handling

Redis is configured to fail loudly rather than silently on memory pressure (`--maxmemory-policy noeviction`, section 3) — a deliberate choice to surface a full Redis as an explicit write failure rather than a silently-evicted security token. No equivalent Postgres-level failure-handling policy (e.g. read replicas, connection-pool exhaustion behavior beyond default HikariCP) is documented.

### Availability

Single-instance PostgreSQL and single-instance Redis in the current local/development topology. No replication, failover, or high-availability configuration is present in `infrastructure/docker/compose.yml` or described elsewhere. This matches the platform's own stated roadmap — production AWS deployment, HA, and operational hardening are named as in-progress objectives of the current PLATFORM V1.0.0 phase, not completed infrastructure.

## 16. Performance Considerations

### Indexing

Explicit, named `CREATE INDEX` statements only — inline/implicit indexing beyond what a `PRIMARY KEY`/`UNIQUE` constraint provides automatically is prohibited by convention (`CODING_CONVENTIONS.md`). Every domain has a dedicated `indexes_<domain>` migration (e.g. `V64__indexes_auth.sql`, `V121__indexes_oauth_and_sync.sql`) added after the domain's tables, predominantly single-column indexes on FK columns and, where a query pattern is known (`integration.sync_jobs`), a composite index matching that access pattern (`(status, available_at)` for worker polling).

### Query Patterns

Not extensively documented beyond the FK-column indexing above. No slow-query analysis, `EXPLAIN`-driven tuning notes, or query-shape documentation was found in the inspected material.

### Caching

Redis is used exclusively for the two ephemeral, security-critical flows in section 3 — it is explicitly not a general-purpose read-through/write-through cache for domain entities as of the current architecture. No cache-aside pattern over `core`/`resource`/`customer` reads is implemented.

### Partitioning

Not implemented. No table shows partitioning (range, hash, or list) in its migration.

### Scaling

Not established beyond the roadmap intent in `CLAUDE.md`/`SERVER_AGENTS.md` (Redis caching, AWS production deployment, multi-tenant platform expansion are named future objectives of the current and next platform phases). No concrete scaling mechanism (read replicas, connection pooling beyond default HikariCP + `commons-pool2` for Redis) is present in the current infrastructure configuration.

## 17. Architectural Constraints

- Domain-owned tables must not be written to by another domain's package — cross-domain writes go through the owning domain's service, or, in the two documented exceptions (`tenant.tenants` via provisioning, `integration.oauth_connections` via the consent flow only), through a specifically designated orchestration bean.
- `audit.*` records are append-only; no domain's service layer implements `update`/`delete` against them.
- `integration.oauth_connections` accepts writes only from the OAuth consent/onboarding flow — the generic CRUD surface's `POST`/`PUT`/list-all were removed specifically to prevent credential injection or cross-user exposure.
- A table's schema assignment is a domain-ownership statement; adding a table to the wrong schema is an ownership error, not a naming preference.
- `webhook`, `billing`, and `analytics` schemas must not receive tables until the platform roadmap actually reaches those phases (`AGENTS.md`).
- Flyway migration version numbers must remain strictly incremental across the whole `db/migration` tree with no gaps, and must never be renumbered below the applied high-water mark (currently v125).
- Every migration mirrored into `server/src/main/resources/db/migration/` must be comment-free; explanatory comments belong only in the `database/init/` copy.
- Redis-held state must never be treated as the system of record — every durable outcome lands in Postgres.
- `tenant_id` must not be assumed present on a `UserPrincipal`, or inferred as a universal isolation column across every schema — see section 11's per-schema breakdown.

## 18. Validation Criteria

The claims in this document are classified below so a future review can mechanically re-verify them against the live schema/codebase rather than trusting this document indefinitely.

| Claim | Type | Status | Evidence |
|---|---|---|---|
| 13 domain schemas + `public` exist, one package per schema | `STRUCTURAL` | VERIFIED | `V2__create_schemas.sql`, `V4__create_organization_schema.sql`, server package tree |
| `core`/`resource` have no `tenant_id` column; isolation is `organization_id` | `STRUCTURAL` | VERIFIED | `V28__core_services.sql`, `V30__core_bookings.sql`, `V35__resources.sql` |
| `customer`/`widget`/`auth.api_keys` isolate directly by `tenant_id` | `STRUCTURAL` | VERIFIED | `V42__customers.sql`, `V47__widgets.sql`, `V62__auth_api_keys.sql` |
| `auth.accounts` no longer stores OAuth tokens | `STRUCTURAL` | VERIFIED | `V113__auth_accounts_drop_oauth_tokens.sql` |
| `auth.user_passwords.raw` (plaintext) column has been dropped | `STRUCTURAL` | VERIFIED — contradicts other project documentation, flagged in §11 | `V114__auth_user_passwords_drop_raw.sql` |
| `audit.*` FKs into `auth`/`widget`/`organization` are deliberately not real FKs | `STRUCTURAL` + `DOCUMENTARY` | VERIFIED | `V58__audit_actors.sql` (no FK constraints on those columns), `SERVER_AGENTS.md` |
| `audit.*` service layers implement no `update`/`delete` | `BEHAVIORAL` | PARTIALLY_VERIFIED — asserted in `SERVER_AGENTS.md`, not re-inspected against current service source in this session | `SERVER_AGENTS.md`'s audit CRUD notes |
| `integration.oauth_connections` is the sole live-credential store for Google | `STRUCTURAL` + `BEHAVIORAL` | VERIFIED | `V115__integration_oauth_connections.sql`, `V118`, `V125`, `SECURITY.md` §12 |
| `integration.oauth_connections` CRUD write endpoints are removed except consent flow | `BEHAVIORAL` | PARTIALLY_VERIFIED — asserted in `SECURITY.md`, controller source not re-read this session | `SECURITY.md` §12 |
| Flyway high-water mark is v125 | `STRUCTURAL` | VERIFIED | Highest version file present in `server/.../db/migration` (`V125__integration_destinations_add_not_null.sql`) |
| `database/init/` mirror is behind the `server/` Flyway copy | `STRUCTURAL` | VERIFIED | Directory listing comparison performed this session — `database/init/integration` tops out at V214/V206 domain-local numbering with no `oauth_connections`/`sync_jobs`/`destination_drives` equivalents |
| No RLS/DB-level privilege separation exists | `SECURITY` | UNVERIFIABLE from migrations alone — no `GRANT`/`REVOKE`/`CREATE POLICY` statements found, but database role configuration outside migrations was not inspected | Migration tree scan this session |
| No backup/recovery strategy is defined | `OPERATIONAL` | UNVERIFIABLE — `database/backup/` directory exists but its contents were not inspected this session | Directory listing only |
| Tenant is optional on `UserPrincipal`; core booking engine works without one | `DOCUMENTARY` + `BEHAVIORAL` | VERIFIED | `SECURITY.md` §5 "Tenant Optionality", corroborated structurally by absence of `tenant_id` on `core.*`/`resource.*` |

Re-running this validation means: for `STRUCTURAL` rows, re-diff the current migration tree against the claims above; for `BEHAVIORAL` rows, re-inspect the named service classes; for `OPERATIONAL`/`SECURITY` rows marked `UNVERIFIABLE`, inspect infrastructure configuration and database role grants outside the migration tree, which this session did not have cause to open.
