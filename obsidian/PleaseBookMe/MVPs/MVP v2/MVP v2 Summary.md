# Context Document for Production Platform v1.0.0 Development

---

## What MVP v2 Was For

MVP v2 took the validated slot-generation engine from MVP v1 and answered the question v1 deliberately left open: **who owns this data, and who is allowed to touch it?**

Where v1 proved that slots can be computed dynamically rather than stored, v2 proved that a real business (not a single anonymous schedule) can own that engine — with a real owner account, real staff accounts, real roles, and a real authentication layer sitting in front of every write operation. The scheduling core itself was carried forward unmodified; v2 wrapped identity, teams, and RBAC around it.

The single most important outcome of v2: a business is not one row, it is a **graph of rows created atomically** — a user, a password, a team, a membership, a role assignment, a profile, a schedule, an availability rule, and a calendar integration, all created together inside one transaction by the **Workspace Provisioning Service**, or none of them are created at all.

---

## Business Context

Same long-term target as v1: lightweight booking infrastructure for small Vietnamese service businesses (salons, dentists, consultants, barbershops) currently running on phone calls and social media DMs.

v2 changes the business model from "one schedule for one anonymous business" to a **service-assisted platform with centralized control**: the company builds and hosts the client's booking site, embeds the reusable widget, and charges a setup fee plus monthly hosting — it is not yet self-service SaaS. That distinction matters architecturally: v2 does not need public self-registration flows, email verification, or billing. It needs a provisioning workflow an operator can run once per new client to stand up a fully working workspace in one shot.

v2 is not the final production architecture either — the codebase openly plans a later move toward NextJS on the frontend and a more service-oriented backend. v2's job was to prove the identity and authorization model, the same way v1 proved the scheduling model.

---

## Technical Stack (MVP v2)

**Backend:**
- Java 21
- Spring Boot 4.0.6 (bundles Jackson 3.x — import `tools.jackson.databind.JsonNode`, not `com.fasterxml.jackson.databind`)
- Spring Data JPA
- Spring Security (stateless JWT, no sessions)
- PostgreSQL (via Docker, port 5433)
- Maven

**Frontend — two separate clients exist side by side:**
- `client_react/` — the original React 19 + Vite + TypeScript + Tailwind v4 booking widget, carried forward from MVP v1 essentially unchanged (no auth, no team awareness).
- `client/` — a newer React 19 + **Next.js 16** + TypeScript + Tailwind v4 rebuild of the same widget, extended with team/staff awareness (`ServiceList`, `StaffList`) and a JWT auth layer (`AuthGate`, `tokenStore`). This is the direction the widget was actively evolving toward by the end of v2.

**Database:**
- PostgreSQL, three schemas this phase: `auth`, `core`, `integration`
- All temporal values UTC (`TIMESTAMPTZ` / `Instant`), same discipline as v1

---

## Domain Model

### Auth Domain (new in v2)

**User** — the platform-wide identity. Holds `username`, `email`, `phone_number`, `bio`, `language`, `timezone`, `week_start`, and `account_status` (`ACTIVE | SUSPENDED | LOCKED`). Also carries `default_schedule_id`, a nullable self-reference into `core.schedules` set once the user's first schedule is provisioned.

**UserPassword** — a one-to-one shadow table keyed by `user_id` itself (no surrogate PK), holding only the bcrypt `hash`. Split out from `User` so the password never travels with a routine user read.

**Team** — the business/workspace boundary. Holds `name`, `slug` (unique), `bio`, `timezone`, `week_start`, and a JSON `metadata` column. Every other business-owned resource ultimately traces back to a team through membership, not through a direct foreign key — `event_types`, `schedules`, and `bookings` reference the *user*, and the user's *membership* determines which team they belong to.

**Profile** — a public-facing identity distinct from the account itself: `username` (the handle used in public booking URLs), scoped to one `user_id` + `team_id` pair.

**Role / Permission / RolePermission** — classic RBAC. `Role` is just `name` + `description`. `Permission` is `resource` + `action` + a unique `slug` (`resource.action`, e.g. `event_types.delete`). `RolePermission` is a plain composite-PK join table.

**UserRole** — assigns *system-level* roles directly to a user (composite PK `user_id, role_id`, plus an `assigned_at` timestamp added by a later migration). This is how the platform-level roles (`PLATFORM_OWNER`, `PLATFORM_ADMIN`) are granted — they are not scoped to any one team.

**Membership / MembershipRole** — the business-level counterpart. `Membership` links one `user_id` to one `team_id` with an `accepted` boolean. `MembershipRole` (composite PK `membership_id, role_id`) is where the *business-level* roles (`BUSINESS_OWNER`, `BUSINESS_MANAGER`, `STAFF`, `MEMBER`, `READ_ONLY`) actually get assigned — a user's authority inside a specific team is a property of their membership in that team, not of the user globally.

**RefreshToken / Session** — `RefreshToken` is the actual credential used by the JWT refresh flow (`secret`, `expires_at`, nullable `revoked_at`). `Session` is a separate, simpler `session_token` + `expires` table that exists in the schema but is not wired into the JWT flow described below — the two were not unified in this phase.

**Attribute** — a team-scoped `name`/`slug` pair with no defined consumer yet in this phase; present as forward-looking schema, same spirit as the `auth.permissions` `SESSION.*` rows reserved ahead of use in the current platform codebase.

### Core Domain (inherited from v1, extended)

Same five core concepts as v1 — **Schedule → Availability → EventType → Booking → SelectedSlot** — carried forward with their v1 semantics intact (slots still computed dynamically, never stored), plus two additions:

- **Schedule** and **Availability** both gained a `user_id` foreign key (v1 schedules were unowned). `Availability.days` is still the `integer[]` column matching `DayOfWeek.getValue()` (1 = Monday … 7 = Sunday).
- **EventType** gained `user_id`, `profile_id`, `team_id`, and `destination_calendar_id` — an event type is now owned by a specific person on a specific team, and can be wired to a calendar integration. It also gained `requires_confirmation` (drives `PENDING` vs `ACCEPTED` at booking time) and `currency` alongside the v1 `price`.
- **Booking** gained `user_id` (the staff member the booking belongs to), `destination_calendar_id`, and both `rejection_reason` and `cancellation_reason` as separate fields. `status` became a genuine Postgres enum this time — `core.booking_status`: `PENDING | ACCEPTED | REJECTED | CANCELLED` — closing the "orphaned enum type" gap flagged at the end of v1. A later migration added soft-delete columns, `deleted_at` + `deleted_by`.
- **Attendee** — new in v2. One row per booking, holding the customer's contact details (`email`, `phone_number`, `name`, `locale`, `timezone`) and a `no_show` flag. This is where v1's flat `customerName`/`customerPhone` fields on `Booking` moved to, decoupled into their own entity.
- **OutOfOffice** — new in v2. A `user_id`-scoped `[start_time, end_time)` block with an optional `reason` and `notes`. Unlike a booking, it is a hard block with no buffer applied around it — see the slot engine section below.
- **SelectedSlot** gained a `user_id` and an `is_seat` flag; its core mechanism (unique `(event_type_id, slot_start, slot_end)` constraint acting as the concurrency lock, `release_at` expiry) is unchanged from v1.

### Integration Domain (new in v2)

**DestinationCalendar** — one row per user per external calendar target: `integration_type` (`GOOGLE_APPS_SCRIPT | GOOGLE_CALENDAR | OUTLOOK`), `external_id`, and `is_app_script_url`. In this phase only the Google Apps Script path is actually wired up — `external_id` stores a deployed Apps Script Web App URL, and the platform POSTs booking events to it. Full OAuth-based Google Calendar / Outlook integration is scaffolded (the enum values exist) but not implemented — that is deferred to a later platform phase.

---

## Database Schema (three schemas)

```text
auth.users              id, uid, username(UNIQUE), name, email(UNIQUE), phone_number, bio,
                         language, timezone, week_start, account_status, default_schedule_id(FK→core.schedules),
                         metadata(JSONB), created_at, updated_at

auth.user_passwords     user_id(PK, FK→auth.users CASCADE), hash, created_at, updated_at

auth.teams              id, name, slug(UNIQUE), bio, metadata(JSONB), timezone, week_start,
                         created_at, updated_at

auth.profiles           id, uid, user_id(FK→auth.users), team_id(FK→auth.teams), username,
                         created_at, updated_at

auth.roles              id, name(UNIQUE), description, created_at, updated_at

auth.permissions        id, name, description, resource, action, slug(UNIQUE),
                         created_at, updated_at

auth.user_roles         user_id(FK→auth.users CASCADE), role_id(FK→auth.roles CASCADE),
                         assigned_at
                         PRIMARY KEY(user_id, role_id)

auth.role_permissions   role_id(FK→auth.roles CASCADE), permission_id(FK→auth.permissions CASCADE)
                         PRIMARY KEY(role_id, permission_id)

auth.memberships        id, team_id(FK→auth.teams), user_id(FK→auth.users), accepted,
                         created_at, updated_at

auth.membership_roles   membership_id(FK→auth.memberships CASCADE), role_id(FK→auth.roles CASCADE),
                         created_at
                         PRIMARY KEY(membership_id, role_id)

auth.refresh_tokens     id, secret(UNIQUE), owner, user_id(FK→auth.users), created_at,
                         expires_at, revoked_at

auth.sessions           id, session_token(UNIQUE), user_id(FK→auth.users), expires

auth.attributes         id, team_id(FK→auth.teams), name, slug, created_at, updated_at

core.schedules          id, user_id(FK→auth.users CASCADE), title, timezone, created_at, updated_at

core.availabilities     id, user_id(FK→auth.users CASCADE), schedule_id(FK→core.schedules CASCADE),
                         days(INTEGER[]), start_time, end_time, created_at, updated_at
                         CHECK(start_time < end_time)

core.event_types        id, title, slug, description, interface_language, length, slot_interval,
                         before_event_buffer, after_event_buffer, user_id(FK→auth.users),
                         profile_id(FK→auth.profiles), team_id(FK→auth.teams),
                         destination_calendar_id(FK→integration.destination_calendars),
                         schedule_id(FK→core.schedules), timezone, price, currency,
                         requires_confirmation, metadata(JSONB), created_at, updated_at
                         CHECK(length > 0), CHECK(slot_interval > 0)

core.bookings           id, uid(UNIQUE), user_id(FK→auth.users), title, description,
                         start_time, end_time, event_type_id(FK→core.event_types),
                         status(core.booking_status DEFAULT 'PENDING'), cancellation_reason,
                         rejection_reason, destination_calendar_id(FK→integration.destination_calendars),
                         deleted_at, deleted_by(FK→auth.users), created_at, updated_at
                         CHECK(start_time < end_time)

core.attendees          id, booking_id(FK→core.bookings CASCADE), email, phone_number, name,
                         locale, timezone, no_show

core.selected_slots     id, event_type_id(FK→core.event_types CASCADE), user_id(FK→auth.users CASCADE),
                         slot_start, slot_end, uid(UNIQUE), release_at, is_seat, created_at
                         UNIQUE(event_type_id, slot_start, slot_end)

core.out_of_office      id, uid(UNIQUE), user_id(FK→auth.users CASCADE), start_time, end_time,
                         reason, notes, created_at, updated_at
                         CHECK(start_time < end_time)

integration.destination_calendars   id, integration(integration.integration_type), external_id,
                                     user_id(FK→auth.users), is_app_script_url, created_at, updated_at
```

**Postgres enums declared this phase:** `auth.week_start` (`SUNDAY | MONDAY`), `auth.account_status` (`ACTIVE | SUSPENDED | LOCKED`), `auth.role_type` (`PLATFORM_OWNER | PLATFORM_ADMIN | BUSINESS_OWNER | BUSINESS_MANAGER | STAFF | MEMBER | READ_ONLY` — a reference enum; the actual `roles` table stores role names as plain rows, seeded to match these values), `core.booking_status` (`PENDING | ACCEPTED | REJECTED | CANCELLED`), `integration.integration_type` (`GOOGLE_APPS_SCRIPT | GOOGLE_CALENDAR | OUTLOOK`).

**A later `general_migration.sql` reshaped a few columns after the fact:** `auth.users.phone` was renamed to `phone_number`; `core.schedules.name` was renamed to `title`; `core.attendees.email` was loosened to nullable while `phone_number` and `timezone` were tightened to required — an explicit acknowledgment that a phone number, not an email, is the reliable contact channel for this market; `auth.user_roles` gained a required `assigned_at`; `core.bookings` gained the soft-delete pair `deleted_at`/`deleted_by`.

`spring.jpa.hibernate.ddl-auto=update` reconciles schema on boot, same as v1, but `database/init/*.sql` remains the authoritative hand-written source of truth for constraints and indexes.

---

## Authentication & Authorization

v2's defining addition. A full stateless JWT layer sits in front of every endpoint except `/api/v1/auth/**` and `/api/v1/slots/**` (slot availability stays public — a customer must be able to see open times without an account).

**Flow:**

```
POST /api/v1/auth/register   → creates User + UserPassword(hash) + an initial RefreshToken
                                 (all three in one @Transactional method)
                              → returns { accessToken, refreshToken }

POST /api/v1/auth/login      → authenticates credentials via Spring Security's
                                 DaoAuthenticationProvider + BCryptPasswordEncoder
                              → issues a fresh token pair

POST /api/v1/auth/refresh    → TokenRefresher rotates the refresh token: the old
                                 token's revoked_at is set, a new one is persisted,
                                 a new pair is returned
```

**Security components (`security/`):**

| Class | Role |
|---|---|
| `SecurityConfig` | Wires the filter chain, `DaoAuthenticationProvider`, `BCryptPasswordEncoder`, `AuthenticationManager` |
| `CustomUserDetailsService` | Loads `User` + `UserPassword` and assembles `AuthenticatedPrincipal` |
| `AuthenticatedPrincipal` | Implements Spring's `UserDetails`; carries `userUid`, roles, and permissions as `GrantedAuthority` |
| `JwtService` | Issues and parses HS256 JWTs; reads `jwt.secret` / `jwt.expiration` from config |
| `JwtTokenVerifier` | The single validation gate — checks expiry **and** that the token's `type` claim is `ACCESS`, so a refresh token can never authenticate a request |
| `JwtAuthenticationFilter` | `OncePerRequestFilter`; calls the verifier, populates `SecurityContext` on success, 401 otherwise |
| `TokenRefresher` | Implements refresh-token rotation as described above |

Roles are exposed as Spring authorities under the prefix `ROLE_<name>` (e.g. `ROLE_BUSINESS_OWNER`); permissions are exposed flat, with no prefix.

Two efficiency details worth keeping in mind: `login()` avoids a second database round trip by reading the already-loaded `AuthenticatedPrincipal` off `Authentication.getPrincipal()` rather than re-querying the user, and by using `userRepository.getReferenceById(...)` (a lazy proxy) when attaching the new refresh token — no extra `SELECT`. A user's richer profile fields (bio, timezone, etc.) are deliberately **not** part of registration — `register()` only establishes identity; enrichment happens afterward through `PUT /api/v1/users/{id}`.

### Role and Permission Model

Two authorization layers, matching the two ownership scopes in the schema:

- **System-level roles** (`PLATFORM_OWNER`, `PLATFORM_ADMIN`) are granted directly on the user via `UserRole` — they are not scoped to any team, because they describe operating the platform itself, not any one business.
- **Business-level roles** (`BUSINESS_OWNER`, `BUSINESS_MANAGER`, `STAFF`, `MEMBER`, `READ_ONLY`) are granted on the *membership* via `MembershipRole` — the same person can hold a different role on two different teams, because the role is a property of "this person, on this team," not of the person alone.

Permissions follow a flat `resource.action` slug convention (`event_types.delete`, `bookings.cancel`, `system.tenants.manage`) grouped into: Auth/Identity, Teams/Memberships, Booking Domain, Integrations, Audit/Visibility (reserved, not yet backed by an audit table this phase), and System/Platform. The seed mapping gives `PLATFORM_OWNER` everything; `BUSINESS_OWNER` full control over their own team's booking domain plus membership management; `STAFF` read/update access to their own bookings and read-only access to schedules/availability; `READ_ONLY` view-only access across the board. The design brief is explicit that this seed is a starting point, not a permanent ceiling — new permissions, custom per-business roles, and a future move toward ABAC are all expected to layer on top without a schema rewrite.

---

## Workspace Provisioning Service

The onboarding entry point for a new business tenant, and the most structurally important addition in v2. A business is never created by inserting one row — it is created by a single composite request (`CreateWorkspaceRequest`) that provisions an owner, an optional list of staff, and every piece of infrastructure each of them needs, all inside **one `@Transactional` method**. If any step fails, nothing is committed — no half-created business is ever left behind.

### Request shape

```
CreateWorkspaceRequest
  ├── team    — name, slug, bio, timezone, weekStart
  ├── owner
  │     ├── ownerUser              — username, password, name, email, phone, bio, language, timezone, weekStart, accountStatus
  │     ├── ownerProfileUsername   — the public handle
  │     ├── ownerBookingConfig[]   — one or more { schedule + availabilities[] + eventTypes[] }
  │     └── ownerIntegrationConfig — destination calendar URL + isAppScriptURL
  └── staffs[]                     — zero or more, same shape as owner
```

### What happens, per member (owner and each staff, in order)

1. `AuthService.register()` creates the `User`, hashed `UserPassword`, and an initial refresh token.
2. The user is enriched with the supplied bio/language/timezone/weekStart/accountStatus and saved.
3. The system-level `USER` role is assigned via `UserRole`.
4. A `Membership` row is created linking the user to the team — saved immediately so its DB-generated ID is available for the next step.
5. The workspace role is assigned via `MembershipRole` — `BUSINESS_OWNER` for the owner, `STAFF` for each staff member.
6. A public `Profile` is created.
7. For each booking config supplied: a `Schedule`, its `Availability` rules, and its `EventType`s are created.
8. The first schedule created (index 0 of the booking-config list) is linked back onto the user as `default_schedule_id`.
9. A `DestinationCalendar` row is created from the integration config.

### Engine components

Each responsibility lives in its own `@Component` under `services/workspace/engine/`: `UserRegistrationEngine` (steps 1–3), `ProvisionTeamConfigurator` (the team itself), `ProvisionMembershipConfigurator` (steps 4–6 — note it must call `membershipRepository.save()` **before** reading the generated membership ID, since the PK is `BIGSERIAL`), `ProvisionBookingConfigurator` (step 7), and `ProvisionIntegrationConfigurator` (step 9). A family of plain, non-persisted `*Provision` Java objects (`WorkspaceProvision`, `OwnerProvision`, `StaffProvision`, `MembershipProvision`, `UserProvision`, `BookingProvision`) carry state between these components and the final response — they exist purely to assemble the response object, never touching the database themselves.

Both `BUSINESS_OWNER` and `STAFF` must already exist as seeded rows in `auth.roles` before provisioning runs, or the role-assignment step fails. This service intentionally excludes email verification, OAuth login, invitation workflows, multi-owner workspaces, billing, and audit logging — all explicitly deferred to later phases.

---

## Appointment Scheduling Service

The single public-facing entry point a customer actually uses to book a slot. Takes one `ScheduleAppointmentRequest` (event type slug, desired start time, customer contact info) and produces a `Booking` + `Attendee` pair inside one `@Transactional` method.

1. **`BookingResolver`** looks up the `EventType` by slug (404 if missing), anchors the customer's plain `LocalDateTime` to the event type's own timezone to compute UTC `start_time`/`end_time`, and sets the booking's status to `PENDING` or `ACCEPTED` depending on whether the event type has `requires_confirmation` set.
2. **`AttendeeResolver`** creates the linked `Attendee`, defaulting `locale` to `"vi"` and `timezone` to `"Asia/Ho_Chi_Minh"` when the customer didn't supply them.

**Known gap, carried forward openly:** `BookingResolver` does **not** call `SlotConflictValidator` or reserve a `SelectedSlot` before saving. The v1 gap — nothing at the write path actually prevents a double booking — was not closed in v2 either. It is now explicitly documented as a gap rather than an oversight, and only the `CREATE` path is implemented; `AppointmentAction.UPDATE`/`DELETE` exist as unused scaffolding.

Notification is deliberately **not** part of this service — it is wired in through a decoupled event listener instead, described next, specifically so a third-party notification failure can never roll back an already-committed booking.

---

## Notification Service (event-driven, async)

Fires once per committed booking, entirely decoupled from the booking transaction itself:

```
AppointmentSchedulingServiceImpl.schedule()
  └── publishes BookingNotificationEvent
        → [booking transaction commits]
        → BookingNotificationListener  (@Async, @TransactionalEventListener(AFTER_COMMIT))
              → TenantNotificationServiceImpl.notify()
                    → BookingNotificationPayloadBuilder.build()
                    → AppScriptNotificationClient.send()
                          → POST { action, booking } to the tenant's Apps Script Web App URL
```

The listener only fires *after* the database commit, on a separate async thread (`@EnableAsync` required) — this is the mechanism that guarantees a Google Apps Script outage can never take down booking creation. `TenantNotificationServiceImpl` silently no-ops if there's no destination calendar, the integration type isn't `GOOGLE_APPS_SCRIPT`, or `is_app_script_url` is false; a failed Apps Script response is logged as a warning, never thrown. `BookingResolver` JOIN FETCHes both `eventType` and `eventType.user` inside the original transaction specifically so the async listener can safely read them after the Hibernate session that created them has already closed.

`BookingNotificationPayloadBuilder` is where every date/time value gets formatted into the event type's own timezone (`dd/MM/yyyy` for date, `HH:mm` for times, plain ISO-8601 UTC for the two full datetime fields) before being handed to the Apps Script endpoint.

---

## Google Apps Script Integration

The actual calendar/sheet sync mechanism for this phase — deliberately lightweight rather than a full Google OAuth2 integration, which is explicitly deferred to a later platform phase. A script (`scripts/google_app_script/`, three files: `Config.gs`, `Utils.gs`, `Main.gs`) is deployed manually as a Web App inside **the client's own Google account**, and its URL is what gets stored in `destination_calendars.external_id`.

On each booking event it: appends/updates/removes a row in an 18-column Google Sheet (columns A–R: status, title, date, times, service, notes, customer contact info, staff contact info, no-show flag, price, currency, the booking UID as the lookup key, the synced Google Calendar event ID, and a sync-status marker), and creates/updates/deletes the matching Google Calendar event. `UPDATE`/`DELETE` locate the right row by scanning column P (`booking_uid`) rather than relying on Apps Script's `ScriptProperties` store, which has a hard 500 KB ceiling and doesn't survive a script redeploy.

---

## Slot Generation Engine (inherited, extended)

The v1 engine's core algorithm — dynamic computation, never storing a slot — is unchanged and still treated as validated. v2 adds one more source of conflict on top of it.

**Generation sequence:**

1. `EventTypeRepository.findBySlugWithScheduleAndUser` — a single JOIN FETCH pulling the event type, its schedule, and its owning user together, avoiding lazy-load surprises later.
2. Derive the day's `[dayStart, dayEnd)` bounds as UTC instants, anchored to the schedule's timezone.
3. Load `Availability` rules for the schedule; skip any whose `days` array doesn't contain the requested day.
4. Load three independent sources of blocking data, all within the same read-only transaction:
   - active bookings overlapping the day (excluding `CANCELLED` and `REJECTED`)
   - active `SelectedSlot` holds (`release_at` still in the future)
   - **`OutOfOffice` periods for the event type's owner that overlap the day — new in v2**
5. `SlotGenerator` walks each applicable availability window in `slot_interval`-minute steps, emitting candidate slots of length `event_type.length`.
6. `SlotConflictValidator` runs three independent overlap checks per candidate; a slot survives only if it clears all three.

| Check | Blocks on | Buffer applied? |
|---|---|---|
| Booking conflict | Any active booking | Yes — `before_event_buffer` before start, `after_event_buffer` after end |
| Selected-slot conflict | Any live hold | No |
| **Out-of-office conflict (new)** | Any OOO period for the owner | No — OOO is a hard block regardless of buffer configuration |

All three use the same interval-intersection predicate as v1: `slotStart < blockEnd && slotEnd > blockStart`. `GET /api/v1/slots` remains fully public — no authentication required — because a customer must be able to browse availability before deciding to book.

---

## REST API Surface

All endpoints under `/api/v1/`.

**Authentication:**

| Endpoint | Auth required |
|---|---|
| POST `/auth/register` | No |
| POST `/auth/login` | No |
| POST `/auth/refresh` | No |

**Auth domain CRUD:** `/users`, `/passwords`, `/sessions`, `/refresh-tokens`, `/roles`, `/permissions`, `/teams`, `/memberships`, `/profiles`, `/attributes` — all require authentication.

**Core domain CRUD:** `/event-types`, `/schedules`, `/availability`, `/bookings`, `/attendees`, `/selected-slots`, `/out-of-office` — all require authentication.

**Integration domain CRUD:** `/destination-calendars` — requires authentication.

**Read-only, public:** GET `/slots?eventTypeSlug=&date=`

**Composite workflows (authenticated):**

| Endpoint | Purpose |
|---|---|
| POST `/workspace` | Provision a complete business workspace (team + owner + optional staff) in one transaction |
| POST `/appointments` | Resolve a booking + attendee from a customer-facing appointment request |

Standard REST conventions across every CRUD resource: `POST` → 201, `GET` → 200, `PUT /{id}` → 200, `DELETE /{id}` → 204 with no body.

---

## Backend Layering Pattern

Every domain still follows the same seven-folder shape v1 established, now applied consistently across three schemas instead of one:

```
<domain>/
  entities/       — @Entity, @Builder, @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor
  dto/            — Java records: XxxRequest.java, XxxResponse.java (field order matches the entity)
  mappers/        — static toEntity() / toResponse(), no framework, no MapStruct
  repositories/   — extends JpaRepository<Entity, BigInteger> (or a composite-key ID class for join tables)
  services/       — interface XxxService.java
  services/impl/  — @Service class XxxServiceImpl.java, every method @Transactional
  controllers/    — @RestController at /api/v1/<resource>, constructor injection only
```

A few conventions specific to this phase: every PK field needs an explicit `@Column(name="id")`, because Spring's naming strategy would otherwise turn `userUid` into `user_id` and collide with the actual foreign-key column; any `JsonNode` field needs `@JdbcTypeCode(SqlTypes.JSON)` since Hibernate 7 can't infer it for Jackson 3 on its own; `UserPasswordEntity` is the first shared-PK/FK entity, using `@Id @Column(name="user_id")` plus `@OneToOne @MapsId`; missing resources throw `ResourceNotFoundException`, duplicate slugs throw `DuplicateSlugException` — the same two exceptions from v1, reused rather than expanded.

---

## Frontend Architecture

### `client_react/` — the legacy widget

Effectively the same single-page, router-less booking widget from v1 (`PICK → FILL_INFO → SUCCESS` state machine, hand-rolled date math, no external date library, Tailwind v4 with no config file). It was not meaningfully extended for auth or teams in this phase — it represents the widget as it stood at the end of v1, kept around as a reference/fallback.

### `client/` — the Next.js rebuild

The actively evolving surface by the end of v2. Built on Next.js 16 + React 19, it reproduces the same booking flow (`BookingWidget`, `ServiceList`, `SlotList`, `CustomerForm`, `SuccessScreen`, a `DayPickerCalendar` built on `react-day-picker` rather than hand-rolled month math) but adds two things v1 never needed:

- **Team awareness** — `useTeam`, `useTeamServices`, `useTeamStaff` hooks and a `StaffList` component, because a booking widget embedded on a real business's site now has to resolve *which* team's event types and staff to show.
- **A JWT auth layer** — `lib/auth/AuthGate.tsx` and `lib/auth/tokenStore.ts`, the client-side counterpart to the backend's stateless JWT system, used for any authenticated surface built on top of the widget (e.g. staff-facing views), separate from the always-public booking flow itself.

Both clients share the same underlying conventions as v1: data fetching confined to hooks, presentational components, prices formatted as VND, and all slot instants treated as UTC and converted to the event type's timezone only for display.

---

## What v2 Inherited and Did Not Redesign

The slot generation core is still the validated foundation. These pieces carried forward from v1 with only additive changes (new `user_id` ownership columns, the new OOO check) rather than a rewrite:

- The slot-generation algorithm itself (`SlotGenerator`, `SlotConflictValidator`, `BufferCalculator`)
- The dynamic-computation philosophy — slots are still never persisted
- The `Schedule → Availability → EventType → Booking → SelectedSlot` domain shape
- UTC-everywhere temporal storage, converted to a display timezone only at the edge

---

## Known Gaps Carried Into the Next Phase

These are the deliberate, explicitly-documented deferrals v2 leaves behind. They are not oversights — the same discipline v1 applied to its own gap list.

### 1. Booking creation still has no conflict validation (the v1 gap, unresolved)

`BookingResolver` computes the booking's time window and saves it directly. It never calls `SlotConflictValidator` and never reserves a `SelectedSlot` hold. The read path (`GET /slots`) correctly hides taken times, but nothing on the write path stops two concurrent booking requests for the same slot from both succeeding. This was the single most important open item at the end of v1, and it remains open at the end of v2 — now with real user ownership sitting on top of it, which raises the stakes.

### 2. Full Google OAuth2 calendar integration is not implemented

`integration.integration_type` already lists `GOOGLE_CALENDAR` and `OUTLOOK` as enum values, but only `GOOGLE_APPS_SCRIPT` is actually wired to working code. The Apps Script approach was a deliberate, lighter-weight stand-in — a script deployed once inside the client's own Google account — chosen specifically to defer the cost of building and maintaining a proper OAuth2 consent flow until a later phase.

### 3. `AppointmentAction.UPDATE` / `DELETE` are unused scaffolding

Only the `CREATE` path through `AppointmentSchedulingService` is implemented. Rescheduling and cancellation as first-class appointment actions do not exist yet.

### 4. Audit logging does not exist yet

The role/permission seed reserves an entire "Audit / Visibility" permission group (`audit_logs.read`, `audit_logs.export`) and multiple roles are granted `audit_logs.read` in the seed mapping — but there is no `audit` schema, no audit entity, and nothing writes to it. This mirrors the same forward-reservation pattern already used for `auth.permissions` rows with no backing implementation.

### 5. `auth.sessions` exists but is disconnected from the JWT flow

A `Session` entity/table with its own `session_token` + `expires` columns exists alongside the JWT `refresh_tokens` mechanism that actually authenticates requests. The two were never unified — `Session` appears to be either an earlier design that was superseded by JWT, or a placeholder for a future session-based surface (e.g. an admin dashboard) that hadn't been built yet.

### 6. No idempotency guard on workspace provisioning

`POST /api/v1/workspace` has no idempotency-key mechanism. A retried or duplicated request would attempt to create a second workspace rather than being recognized as a repeat of the first. The provisioning design doc names this explicitly as a future improvement, not a current guarantee.

### 7. Two frontends, one clearly ascendant

`client_react/` was not deleted, but active development had visibly moved to the Next.js `client/`. Anyone picking this codebase back up should treat `client/` as the real frontend and `client_react/` as historical reference only.

---

## What the Next Phase Needs to Introduce

Based on where v2 left off, the next phase (Production Platform v1.0.0) needs to:

1. **Close the booking conflict gap** — wire `SelectedSlot` reserve/release, or a DB-level `SELECT FOR UPDATE`, into the actual write path. This is no longer optional once real businesses depend on it.
2. **Replace the Apps Script bridge with real Google OAuth2** — delegated calendar/sheets access per the platform's now-current OAuth2 architecture, rather than a manually deployed script per client.
3. **Introduce a real audit domain** — the permission slugs already exist; the schema and write path do not.
4. **Resolve or remove the `auth.sessions` table** — decide whether it becomes a real session store for a future authenticated surface, or gets dropped as superseded scaffolding.
5. **Add idempotency to workspace provisioning** before it is exposed to anything beyond a trusted internal operator.
6. **Formally retire `client_react/`** once the Next.js client reaches parity, rather than maintaining two frontends in parallel.

The identity model (users, teams, memberships, RBAC), the provisioning workflow, and the booking engine underneath all carry forward as validated foundations — the next phase builds production hardening and real integrations on top of them, the same way v2 built identity on top of v1's engine.

---

## Sample Data

`testing/workspace/sample_request.json` is a complete, realistic `CreateWorkspaceRequest` for a barbershop ("Sonny Barbershop") with one owner and two staff members, each with their own schedule, weekday/weekend availability split, two event types (Haircut, Hair Dying) at different prices, and their own Google Apps Script destination calendar URL. `testing/workspace/response.json` and `request_2.json` sit alongside it; `testing/test_cases.json` holds a broader set of recorded test cases. Together these are sufficient to exercise the full provisioning → booking → notification pipeline end to end without a UI.
