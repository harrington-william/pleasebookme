In a domain-oriented architecture, `application/` and `infrastructure/` exist because a domain module usually needs to separate **business rules**, **use-case orchestration**, and **technical implementation**.

The simplest mental model is:

> **domain = what the business is**  
> **application = what the system does**  
> **infrastructure = how the system technically does it**

For your PleaseBookMe architecture, this distinction is highly useful.

# 1. The three layers

Suppose you have:

```text
booking/
├── domain/
├── application/
└── infrastructure/
```

They have different responsibilities.

```text
                    Booking Module
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
     domain         application     infrastructure
        │                │                │
   Business rules    Use cases       Technical details
   Entities          Commands        PostgreSQL
   Value objects     Queries         Redis
   Domain services   Orchestration   Google Calendar
   Domain events     DTOs            RabbitMQ
```

A useful dependency direction is:

```text
infrastructure ───────► application ───────► domain
       │
       └────────────────────────────────────► domain
```

The domain should **not depend on infrastructure**.

---

# 2. `domain/` — business truth

The domain contains concepts that exist because the **business exists**, not because you're using Spring Boot, FastAPI, PostgreSQL, Redis, etc.

For Booking:

```text
booking/
└── domain/
    ├── model/
    │   ├── Booking
    │   ├── BookingStatus
    │   ├── BookingId
    │   └── ...
    │
    ├── service/
    │   ├── BookingAvailabilityService
    │   └── ...
    │
    ├── repository/
    │   └── BookingRepository
    │
    └── event/
        ├── BookingCreated
        └── BookingCancelled
```

For example:

```text
Booking
```

might enforce:

```text
A cancelled booking cannot be accepted.
An accepted booking cannot be cancelled under certain policies.
A booking must have a valid service.
```

Those are business rules.

The domain shouldn't care whether the booking came from:

```text
REST API
GraphQL
CLI
message queue
admin panel
widget
```

Nor should it care whether persistence uses:

```text
PostgreSQL
MySQL
MongoDB
```

---

# 3. `application/` — use cases

This is where many developers initially get confused.

The application layer answers:

> **What does the system need to do with the domain?**

It coordinates the domain to perform a **use case**.

For example:

```text
booking/application/
├── command/
│   ├── CreateBooking
│   ├── CancelBooking
│   ├── AcceptBooking
│   └── RescheduleBooking
│
├── query/
│   ├── GetBooking
│   └── ListBookings
│
└── service/
    └── BookingApplicationService
```

Imagine:

```text
POST /bookings
```

The controller shouldn't contain the entire booking workflow.

Instead:

```text
HTTP Request
     │
     ▼
BookingController
     │
     ▼
CreateBookingUseCase
     │
     ├── authorize
     ├── load service
     ├── load customer
     ├── validate availability
     ├── create Booking
     ├── save Booking
     └── publish BookingCreated
```

That's application-layer orchestration.

---

# 4. Application layer is NOT business logic dumping ground

This distinction is important.

Don't do:

```text
BookingApplicationService
    ├── 500 lines of business rules
    ├── pricing calculations
    ├── booking state machine
    ├── authorization rules
    └── database SQL
```

Instead:

```text
Application
    │
    ├── orchestrates
    │
    ▼
Domain
    │
    └── enforces business rules
```

For example:

```text
CreateBookingUseCase
```

can say:

```text
1. Load service.
2. Check authorization.
3. Ask availability service whether slot is available.
4. Create Booking.
5. Persist Booking.
6. Publish event.
```

But:

```text
Can a CANCELLED booking become ACCEPTED?
```

belongs to the domain.

---

# 5. `infrastructure/` — technical implementation

Infrastructure contains things that connect your application to the outside world.

For Booking:

```text
booking/
└── infrastructure/
    ├── persistence/
    │   ├── PostgresBookingRepository
    │   ├── BookingEntity
    │   └── BookingMapper
    │
    ├── messaging/
    │   └── RabbitMQBookingEventPublisher
    │
    ├── external/
    │   └── GoogleCalendarBookingSynchronizer
    │
    └── configuration/
        └── BookingConfiguration
```

This is where PostgreSQL, Redis, RabbitMQ, Google APIs, HTTP clients, etc. live.

For example, the domain might define:

```text
BookingRepository
```

as an abstraction.

Infrastructure implements it:

```text
BookingRepository
        ▲
        │ implements
        │
PostgresBookingRepository
```

The domain doesn't know that PostgreSQL exists.

---

# 6. Why do we need the repository interface?

This is one of the most important ideas.

Your domain might define:

```text
interface BookingRepository {
    Booking findById(BookingId id);
    void save(Booking booking);
}
```

Then infrastructure provides:

```text
PostgresBookingRepository implements BookingRepository
```

So:

```text
             DOMAIN
               │
               │ defines
               ▼
       BookingRepository
               ▲
               │ implements
               │
       ┌───────┴────────┐
       │                │
PostgresRepository   InMemoryRepository
```

The business logic depends on the **abstraction**, not PostgreSQL.

This is commonly associated with **Dependency Inversion** and **Hexagonal/Clean Architecture**.

---

# 7. Concrete example

Suppose the user wants to cancel a booking.

Your architecture could look like:

```text
HTTP
 │
 ▼
BookingController
 │
 ▼
CancelBookingUseCase
 │
 ├── AuthorizationEngine
 │
 ├── BookingRepository
 │
 └── Booking.cancel()
          │
          ▼
       Booking
          │
          └── BookingCancelled event
                    │
                    ▼
             Event Publisher
                    │
                    ▼
                RabbitMQ
```

Now map each component:

### Interface/API

```text
booking/api/
└── BookingController
```

### Application

```text
booking/application/
└── CancelBookingUseCase
```

### Authorization

```text
booking/authorization/
└── BookingPolicy
```

### Domain

```text
booking/domain/
└── Booking
```

### Infrastructure

```text
booking/infrastructure/
├── persistence/
│   └── PostgresBookingRepository
└── messaging/
    └── RabbitMQEventPublisher
```

That's a very clean separation.

---

# 8. Where does `api/` fit?

You might therefore end up with:

```text
booking/
├── domain/
├── application/
├── authorization/
├── infrastructure/
└── api/
```

Each has a very specific purpose:

|Package|Responsibility|
|---|---|
|`domain/`|Business concepts and invariants|
|`application/`|Use cases and orchestration|
|`authorization/`|Authorization rules for Booking|
|`infrastructure/`|Technical implementations|
|`api/`|HTTP/API interface|

This is essentially **vertical module organization with internal architectural layers**.

---

# 9. Example with your PleaseBookMe project

Imagine your Booking module:

```text
booking/
│
├── domain/
│   ├── model/
│   │   ├── Booking
│   │   ├── BookingStatus
│   │   └── BookingId
│   │
│   ├── service/
│   │   └── BookingDomainService
│   │
│   ├── repository/
│   │   └── BookingRepository
│   │
│   └── event/
│       ├── BookingCreated
│       ├── BookingCancelled
│       └── BookingRescheduled
│
├── application/
│   ├── command/
│   │   ├── CreateBooking
│   │   ├── CancelBooking
│   │   └── RescheduleBooking
│   │
│   ├── query/
│   │   ├── GetBooking
│   │   └── ListBookings
│   │
│   └── dto/
│       ├── BookingResponse
│       └── BookingDetails
│
├── authorization/
│   └── BookingPolicy
│
├── infrastructure/
│   ├── persistence/
│   │   ├── BookingEntity
│   │   ├── BookingJpaRepository
│   │   └── BookingRepositoryImpl
│   │
│   ├── messaging/
│   │   └── RabbitMQEventPublisher
│   │
│   └── google/
│       └── GoogleCalendarAdapter
│
└── api/
    ├── BookingController
    └── BookingRequest
```

This is a strong structure for a modular monolith.

---

# 10. One important correction to the mental model

Don't think:

```text
domain/
application/
infrastructure/
```

as simply three folders where you arbitrarily distribute classes.

Think:

```text
                 BUSINESS
                    │
                    ▼
                DOMAIN
                    ▲
                    │
               APPLICATION
                    ▲
                    │
              INFRASTRUCTURE
```

More accurately:

```text
                     ┌──────────────┐
                     │     API      │
                     └──────┬───────┘
                            │
                            ▼
                     ┌──────────────┐
                     │ APPLICATION  │
                     │   Use Cases  │
                     └──────┬───────┘
                            │
                            ▼
                     ┌──────────────┐
                     │    DOMAIN    │
                     │ Business     │
                     │ Rules        │
                     └──────────────┘
                            ▲
                            │
                     ┌──────┴───────┐
                     │INFRASTRUCTURE│
                     │ DB / MQ / API│
                     └──────────────┘
```

The **domain is the center**.

Infrastructure is replaceable.

---

# 11. How this relates to your previous authorization question

This is exactly why I suggested:

```text
tenant/
├── domain/
├── application/
├── authorization/
└── infrastructure/
```

rather than:

```text
security/
└── authorization/
    ├── TenantPolicy
    ├── BookingPolicy
    └── ResourcePolicy
```

Because the Tenant module becomes responsible for its own **business boundary**.

You can then have:

```text
security/
└── authorization/
    ├── AuthorizationEngine
    ├── AuthorizationContext
    ├── Permission
    └── AuthorizationDecision
```

and:

```text
tenant/
└── authorization/
    └── TenantPolicy
```

The engine provides the **mechanism**.

The Tenant policy provides the **domain-specific rules**.

That's a particularly good fit for the **RBAC + ABAC + resource relationship** authorization architecture you've been designing for PleaseBookMe.