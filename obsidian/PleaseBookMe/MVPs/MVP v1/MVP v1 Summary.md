# Context Document for MVP v2 Development

---

## What MVP v1 Was For

MVP v1 was a learning-and-validation phase, not a production application.
The goal was to deeply understand the scheduling domain before building anything real.

The single most important outcome: **slots are never stored**.
They are computed dynamically from rules every time a customer requests them.
This is the architectural insight that everything else in the system is built around.

---

## Business Context

Long-term target: lightweight booking websites for small businesses in Vietnam
(salons, dentists, consultants, beauty services) who currently manage appointments
manually via phone or social media.

The platform will eventually be:
- Centralized booking infrastructure
- Reusable booking widget per business
- Template-based website service with embedded booking

MVP v1 only validated the scheduling engine. It has no concept of multi-tenancy,
ownership, or users. That is MVP v2's job.

---

## Technical Stack (MVP v1)

**Backend:**
- Java 21
- Spring Boot 4.0.6
- Spring Data JPA
- PostgreSQL (via Docker, port 5433)
- Jakarta Bean Validation
- `@EnableScheduling` for the SelectedSlot cleanup job

**Frontend:**
- React 19
- Vite
- TypeScript
- Tailwind CSS v4 (`@tailwindcss/vite` plugin — NO tailwind.config.js, NO PostCSS)
- Axios (base URL: `http://localhost:8080/api/v1`)

**Database:**
- PostgreSQL
- Schema: `core`
- All temporal values stored as UTC (`TIMESTAMPTZ` / `Instant`)

---

## Domain Model (Core Concepts)

### Schedule
Reusable availability profile. Owns a timezone (IANA string, e.g. `Asia/Ho_Chi_Minh`)
and a name. This is the source of truth for timezone in slot generation.

### Availability
Recurring working-hour rules attached to a Schedule.
- `days`: PostgreSQL `integer[]` where `1 = Monday … 7 = Sunday`
  (matches `java.time.DayOfWeek.getValue()`)
- `startTime` / `endTime`: `LocalTime` (wall-clock in the schedule's timezone)

### EventType
The booking configuration object — NOT a transaction.
Controls how bookings behave: duration, slot interval, before/after buffers, price.
Has its own `timezone` field (used by the frontend for display) AND a `@ManyToOne`
to a Schedule (used by the backend for slot computation).

**Important drift risk:** EventType.timezone and Schedule.timezone are independent
fields. The slot engine reads `eventType.getSchedule().getTimezone()`. The frontend
reads `eventType.timezone`. These must be kept consistent or slot display will be
wrong.

### Booking
The official transaction record. Fields:
- `uid` (UUID, auto-generated)
- `title` (derived: `customerName + " - " + eventType.title`)
- `startTime` / `endTime` (UTC `Instant`)
- `customerName`, `customerPhone`, `description`
- `status`: `PENDING | CONFIRMED | CANCELLED`
  Stored as `VARCHAR(50)` (was changed from a Postgres ENUM type mid-development
  to avoid JPA cast friction; enforced at application layer via Java enum with
  `@Enumerated(EnumType.STRING)`)

### SelectedSlot
Temporary reservation mechanism for concurrency control.
- `uid`, `eventType`, `slotStart`, `slotEnd`, `releaseAt`, `createdAt`
- DB enforces `UNIQUE(event_type_id, slot_start, slot_end)` — this is the actual lock
- Auto-cleanup: `@Scheduled(fixedDelay = 60_000)` deletes expired rows every 60 seconds
- Reservation window: 5 minutes

---

## Database Schema (`core` schema)

```
schedules           id, name, time_zone, created_at, updated_at
availability        id, schedule_id(FK→schedules CASCADE), days(integer[]),
                    start_time, end_time, created_at, updated_at
event_types         id, title, slug(UNIQUE), description, length, slot_interval,
                    before_buffer_minutes, after_buffer_minutes, schedule_id(FK→schedules RESTRICT),
                    time_zone, price(NUMERIC 12,2), created_at, updated_at
bookings            id, uid(UNIQUE), title, description, start_time, end_time,
                    event_type_id(FK→event_types RESTRICT), customer_name, customer_phone,
                    status(VARCHAR 50 DEFAULT 'PENDING'), created_at, updated_at
selected_slots      id, uid, event_type_id(FK→event_types CASCADE), slot_start, slot_end,
                    release_at, created_at
                    UNIQUE(event_type_id, slot_start, slot_end)
```

**Note:** `core.booking_status` Postgres ENUM type is still declared in `database/init.sql`
but is orphaned — the `bookings.status` column does not use it. The `BookingEntity` still
carries a stale `columnDefinition = "core.booking_status"` annotation which is harmless
but should be cleaned up in MVP v2.

`spring.jpa.hibernate.ddl-auto=update` — Hibernate reconciles schema on startup.
`database/init.sql` is the authoritative hand-written schema (source of truth for
constraints, indexes, and `integer[]` columns).

---

## REST API Surface

All endpoints under `/api/v1/`.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/schedules` | Create schedule → 201 |
| GET | `/schedules` | List all schedules |
| POST | `/event-types` | Create event type → 201 |
| GET | `/event-types` | List all event types |
| GET | `/event-types/{slug}` | Get event type by slug |
| POST | `/availability` | Create availability rule → 201 |
| GET | `/availability` | List all availability rules |
| GET | `/slots?eventTypeSlug=&date=` | Compute available slots (date: `YYYY-MM-DD`) |
| POST | `/bookings` | Create booking → 201 |

No SelectedSlot REST endpoint exists. The reservation mechanism is service-layer only.

CORS: `WebConfig.java` allows any `http://localhost:*` and `http://127.0.0.1:*` origin
on `/api/**`. Dev convenience only — must be restricted in production.

---

## The Slot Generation Engine (Most Important Part)

Slots are **never stored**. They are computed on every `GET /slots` request.

### Orchestration (`SlotServiceImpl.getAvailableSlots`)

1. Load EventType by slug → 404 if absent
2. Derive `zone` from `eventType.getSchedule().getTimezone()`
3. Derive `dayOfWeek` from the requested date
4. Load all Availability rules for the schedule
5. Compute UTC day bounds: `dayStart = date.atStartOfDay(zone)`, `dayEnd = date+1.atStartOfDay(zone)`
6. Load non-cancelled bookings within `[dayStart, dayEnd)` from `BookingRepository`
7. Load active SelectedSlot holds for the event type (`releaseAt > now`)
8. For each availability rule whose `days` contains `dayOfWeek.getValue()`:
   - Generate candidate slots
   - Filter through conflict validator
9. Return `AvailableSlotsResponse(eventTypeSlug, date, slots)`

### Candidate Generation (`SlotGenerator.generateSlots`)

- Converts `LocalTime` availability bounds → UTC `Instant` via `TimeZoneConverter`
- Steps from window start by `slotInterval` minutes
- Emits a slot only if `slotStart + length <= windowEnd` (slot fits entirely in window)
- `length` (appointment duration) and `slotInterval` (cadence) are independent —
  30-min appointments every 15 min produces overlapping start times

### Conflict Filtering (`SlotConflictValidator.isSlotAvailable`)

A slot is available iff it overlaps neither an existing booking nor an active hold.

**Booking conflict (with buffers):**
```
blockedStart = bookingStart − beforeBufferMinutes
blockedEnd   = bookingEnd   + afterBufferMinutes
conflict = slot.start < blockedEnd && slot.end > blockedStart
```

**Hold conflict (no buffer):**
```
conflict = slot.start < hold.slotEnd && slot.end > hold.slotStart
```

**Worked example:** duration=30, interval=30, afterBuffer=15, window=09:00–10:30,
existing booking 09:00–09:30.
Candidates: 09:00, 09:30, 10:00.
Booking blocks [09:00, 09:45] (incl. 15-min after buffer).
→ 09:00 conflicts, 09:30 conflicts (starts inside buffer), 10:00 is free.
Result: **[10:00–10:30]**.

### Engine Architecture

`SlotGenerator`, `SlotConflictValidator`, `BufferCalculator` are **plain POJOs** —
not Spring beans. They are instantiated directly inside `SlotServiceImpl`.
Stateless, pure, unit-test friendly.

---

## Backend Layering Pattern

Every domain module follows the same structure:

```
Controller (@RestController, /api/v1/...)      ← HTTP, @Valid request bodies
  → Service (interface in services/)
    → ServiceImpl (@Service in services/impl/) ← business logic, @Transactional
      → Repository (Spring Data JPA)           ← persistence
      → Mapper (static toEntity / toResponse)  ← DTO ↔ entity (no MapStruct)
Entity (@Entity, schema = "core")              ← JPA persistence model
DTOs: *Request (inbound, validated) / *Response (outbound)
```

Services are split interface + `*ServiceImpl` even for single implementations —
intentional seam for future NestJS port.

---

## Global Error Handling

`GlobalExceptionHandler` (@ControllerAdvice) maps to `ApiErrorResponse`:

| Exception | HTTP |
|-----------|------|
| `ResourceNotFoundException` | 404 |
| `SlotNotAvailableException` | 409 |
| `InvalidSlotException` | 400 |
| `DuplicateSlugException` | 409 |
| `MethodArgumentNotValidException` | 400 + field map |
| `DataIntegrityViolationException` | 409 |

`ApiErrorResponse` shape: `{ status, message, data, client, timestamp, path }`

---

## Frontend Architecture

Single-page booking widget. No router. `App.tsx` renders `BookingPage` directly.

### 3-Step State Machine in `BookingPage.tsx`

```
PICK  → customer selects service (EventType), date, and time slot
FILL_INFO → customer enters name, phone, optional description
SUCCESS → booking confirmed (status=PENDING)
```

### Key Files

```
src/api/
  axios.ts          base URL http://localhost:8080/api/v1
  eventTypeApi.ts   getEventTypes(), getEventTypeBySlug(slug)
  slotApi.ts        getAvailableSlots(eventTypeSlug, date)
  bookingApi.ts     createBooking(request)

src/hooks/
  useEventTypes.ts  loads services once on mount
  useSlots.ts       refetches when service/date change

src/components/
  calendar/MonthCalendar.tsx   month grid, Monday-first, dims past/adjacent days
  slots/SlotGrid.tsx           time-slot buttons (handles loading/error/empty states)
  booking/CustomerForm.tsx     name/phone/note form; exports CustomerFormValues
  common/ServiceSelector.tsx   EventType dropdown
  common/Spinner.tsx
  common/ErrorMessage.tsx

src/utils/dateUtils.ts         month-grid math by hand; Intl.DateTimeFormat for display
src/types/index.ts             TypeScript interfaces mirroring backend DTOs
```

### Frontend Conventions

- No external date library. All date math is `dateUtils.ts` + `Intl.DateTimeFormat`.
- Slot instants are UTC; displayed in `eventType.timezone` so customer sees local time.
- Price formatted as Vietnamese đồng (`vi-VN` / `VND`); null/0 = "Free".
- Data fetching in hooks only; components are presentational.

---

## TypeScript ↔ Backend Contract (`src/types/index.ts`)

```typescript
EventType        { eventTypeId, title, slug, description, length, slotInterval,
                   beforeBufferMinutes, afterBufferMinutes, scheduleId,
                   timezone, price }
TimeSlot         { slotStart: string, slotEnd: string }  // UTC ISO instants
AvailableSlotsResponse  { eventTypeSlug, date, slots: TimeSlot[] }
BookingRequest   { eventTypeSlug, startTime, customerName, customerPhone, description? }
BookingResponse  { uid, startTime, endTime, status, ... }
BookingStatus    'PENDING' | 'CONFIRMED' | 'CANCELLED'
```

---

## Infrastructure

```bash
# Start PostgreSQL (from infrastructure/docker/)
docker compose --env-file .env up -d
# .env sets DB_NAME, DB_USER, DB_PASSWORD, DB_PORT (default 5433)

# Backend (from server/)
./mvnw spring-boot:run

# Frontend (from client/)
npm run dev
```

`spring.docker.compose.enabled=false` — do NOT auto-start the dev `compose.yaml`.
Use the `infrastructure/docker/` Postgres for a persistent local database.

Default datasource: `jdbc:postgresql://localhost:5433/pleasebookme_db`
User: `harrington`, password: `DeveloperPassword123`

---

## Known Gaps Carried Into MVP v2

These are intentional deferrals from MVP v1, not oversights. MVP v2 must be aware of them.

### 1. Booking creation has no conflict validation (CRITICAL)
`BookingServiceImpl.createBooking` loads the event type and saves directly.
It does NOT check if the requested slot is actually available, does NOT consume a
SelectedSlot hold, and does NOT apply buffer logic.

Double bookings are currently possible. The slot engine correctly hides taken slots
on the read path, but nothing prevents two concurrent POST /bookings for the same
time from both succeeding.

**What was built but not wired:** `SelectedSlotServiceImpl` has `reserve()` and
`release()` fully implemented. `BookingRepository` has an overlap query declared
(`findByEventType_EventTypeIdAndStartTimeLessThanAndEndTimeGreaterThan`). Neither is
called from `BookingServiceImpl`.

MVP v2 should wire this or replace it with a DB-level transaction + SELECT FOR UPDATE
approach once the auth/ownership layer is introduced.

### 2. SelectedSlot has no REST endpoint
The reservation mechanism exists at the service layer but cannot be triggered via HTTP.
The intended flow (customer selects slot → POST reserve → fill form → POST booking →
slot hold consumed) is not implemented end-to-end.

### 3. Inconsistent not-found handling
`BookingServiceImpl` and some paths of `EventTypeServiceImpl` throw bare
`RuntimeException` → HTTP 500 where `ResourceNotFoundException` → 404 is intended.

### 4. Two timezone sources can drift
`EventType.timezone` (used by frontend for display) and `Schedule.timezone` (used by
backend slot engine) are independent fields. They are assumed consistent but nothing
enforces it. A booking created in the wrong timezone interpretation would silently
misbehave.

### 5. Stale Postgres ENUM artifact
`database/init.sql` still declares `CREATE TYPE core.booking_status AS ENUM (...)`.
`BookingEntity.status` still carries `columnDefinition = "core.booking_status"`.
The column is actually `VARCHAR(50)`. The `columnDefinition` is harmless (overridden
by `EnumType.STRING`) but misleading.

---

## What MVP v2 Inherits and Should Not Touch

The slot generation engine is the validated core. These files should be reused as-is
unless a specific bug is found:

- `slot/engine/SlotGenerator.java`
- `slot/engine/SlotConflictValidator.java`
- `slot/engine/BufferCalculator.java`
- `slot/services/impl/SlotServiceImpl.java`
- `global/utils/TimeZoneConverter.java`
- `selectedSlot/services/impl/SelectedSlotServiceImpl.java` (the mechanism is correct;
  only the wiring to the booking flow is missing)

The domain model (Schedule → Availability, EventType, Booking, SelectedSlot) is
validated. MVP v2 adds identity and ownership on top of it without redesigning the
scheduling core.

---

## What MVP v2 Needs to Introduce

Based on the roadmap, MVP v2 introduces:

1. **User identity** — user table, registration, login, JWT or session tokens
2. **Ownership** — EventType and Schedule need an owner (user or organization)
3. **Tenant isolation** — data must be scoped per business/owner
4. **RBAC** — roles (admin, staff, viewer) with permission checks
5. **Protected endpoints** — all write operations need authentication; slot/booking
   reads may remain public (customers booking without accounts)
6. **Booking conflict validation** — wire SelectedSlot reserve/release into
   the booking creation flow (this is now critical because real users own real slots)

The booking engine, database schema (minus new auth tables), and frontend widget
architecture are all carried forward.

---

## Sample Data

The repository includes `database/sameple_data.sql` with one working seed:
- Schedule: "Sample Weekday Schedule" (Asia/Ho_Chi_Minh)
- Availability: Mon–Fri 09:00–17:00
- EventType: Haircut, 30-min duration, 30-min interval, 15-min before/after buffer,
  price 100,000 VND

This seed is sufficient to test the full slot generation flow end-to-end.
