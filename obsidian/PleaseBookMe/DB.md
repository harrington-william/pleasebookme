Harrington, after reviewing:

- Your updated architecture diagram
    
- Your production roadmap
    
- Your long-term business orientation
    
- Your domain architecture
    

I think the architecture has become **much more coherent** than the previous iterations. It now clearly separates:

- Infrastructure
    
- Business capabilities
    
- Future platform services
    
- Operational roadmap
    

and, most importantly, **the database is beginning to reflect the business rather than the UI**. That is exactly the direction you want for a long-lived SaaS platform.

---

# Overall Review

I would score the current database around

> **9.2 / 10**

Not because tables are missing.

But because I think several domains still mix responsibilities.

The biggest ones are:

1. Booking Policy
    
2. Tenant
    
3. Widget
    

Those three domains are actually much larger than they currently look.

---

# 1. Booking Policy

This is actually the heart of your infrastructure.

This table determines whether your platform can support

- barber
    
- hotel
    
- coworking
    
- tennis court
    
- classroom
    
- meeting room
    
- equipment rental
    
- vehicle rental
    

without redesigning Core.

Right now it is still appointment-centric.

---

## I would redesign it like this

```
booking_policies
----------------
id

service_id

booking_mode

resource_mode

duration_type

default_duration

minimum_duration

maximum_duration

slot_interval

minimum_notice

maximum_advance_booking

before_buffer

after_buffer

allow_overlap

allow_multiple_attendees

capacity

auto_confirm

allow_cancellation

allow_reschedule

requires_payment

booking_window_type

metadata

created_at
updated_at
```

---

## New enums

### Duration Type

Instead of

```
FIXED
FLEXIBLE
HYBRID
```

I would split the concepts.

```
DurationType

FIXED

FLEXIBLE

HYBRID
```

means

Who determines duration?

---

Then

### Resource Mode

```
SINGLE

MULTIPLE

CAPACITY
```

Examples

Haircut

```
1 barber

SINGLE
```

Hotel

```
Room

SINGLE
```

Meeting room

```
Room

CAPACITY
```

Gym class

```
50 seats

CAPACITY
```

Equipment rental

```
Multiple items

MULTIPLE
```

Now Core never needs to know business type.

---

## Booking Window

```
booking_window_type

UNLIMITED

ROLLING

FIXED_RANGE
```

Examples

Restaurant

```
30 days ahead
```

Hotel

```
1 year
```

Doctor

```
90 days
```

---

## Minimum Notice

Instead of

```
before_buffer
```

you need

```
minimum_notice

2 hours

1 day

30 minutes
```

This determines

"How late can customers book?"

---

## Maximum Advance Booking

```
365 days

180 days

30 days
```

Hotel uses this constantly.

---

## Capacity

```
capacity

1

2

50

100
```

Then one service supports

Conference

without redesign.

---

## Requires Payment

Future billing.

```
requires_payment

true

false
```

---

# Missing Cancellation Policies

I would not put these inside booking_policies.

Instead

```
booking_cancellation_policies
```

because eventually people want

```
Cancel until

24 hours

12 hours

2 hours
```

Penalty

Refund %

etc.

Separate concern.

---

# Reschedule Policy

Also separate.

```
booking_reschedule_policies
```

Because

Hotels

Doctors

Courts

have very different rules.

---

# 2. Tenant Schema

I think Tenant is currently too small.

It currently looks like

```
Tenant

name

ip

owner
```

That's not really what a SaaS tenant is.

---

I'd redesign

```
tenants

id

uid

organization_id

owner_user_id

name

slug

status

plan_id

region

default_timezone

default_locale

max_users

max_services

max_widgets

settings

created_at

updated_at
```

---

## Tenant Status

```
ACTIVE

SUSPENDED

TRIAL

PENDING

ARCHIVED
```

---

## Region

Future

```
VN

AU

SG
```

important later

---

## Settings

JSON

Future-proof

```
branding

logo

primary color

feature flags

etc
```

---

## Remove

```
ip_address
```

I would remove it.

A tenant does not have one IP.

Widgets

Dashboard

API

Employees

Customers

all use different IPs.

Wrong abstraction.

---

# Tenant Domains Missing

I would create

```
tenant_domains
```

```
id

tenant_id

domain

verified

is_primary

verification_token

created_at
```

Examples

```
booking.mybarber.vn

booking.hotel.com

appointments.company.com
```

Necessary for widgets.

---

# Tenant Plans

Separate table

```
tenant_plans
```

```
FREE

STARTER

PRO

ENTERPRISE
```

Future Billing.

---

# Feature Flags

```
tenant_features

tenant_id

feature

enabled
```

Examples

Analytics

SMS

API

AI

Webhooks

---

# 3. Widget Schema

This domain is much larger.

Currently

```
Widget

tenant

ip

created
```

This isn't enough.

---

I'd build

```
widgets

id

uid

tenant_id

service_id

name

status

type

origin_validation

public_key

secret_key

issued_at

expires_at

last_used_at

created_at

updated_at
```

---

## Widget Type

```
INLINE

POPUP

FULL_PAGE

EMBEDDED
```

---

## Widget Status

```
ACTIVE

DISABLED

REVOKED
```

---

## Widget Origin

Very important.

```
allowed_origins
```

Separate table

```
widget_origins

id

widget_id

origin

created_at
```

Example

```
https://barber.com

https://booking.barber.com
```

Then

API Gateway

checks

```
Origin

Widget JWT

Tenant
```

Exactly like your architecture.

---

# Widget Credentials

You already wrote

```
WidgetJWT
WidgetUID
TenantID
```

inside request.

I would formalize.

```
widget_credentials

widget_id

public_key

secret_hash

algorithm

expires_at

rotated_at
```

Future key rotation.

---

# Widget Token

Future

```
widget_token

contains

widget_uid

tenant_uid

issued_at

expires

signature
```

Exactly like Stripe publishable keys.

---

# Widget Analytics

Later

```
widget_events

VIEW

OPEN

BOOKING_STARTED

BOOKING_COMPLETED

ERROR
```

Don't mix with analytics schema.

Just emit Kafka events.

---

# One More Thing Missing

I think you're missing an entire schema.

---

# Resource Schema

This is what allows hotels.

Right now

```
Service
```

does too much.

Instead

```
resource
```

---

Example

```
Haircut

↓

Barber A

Barber B

Barber C
```

---

Hotel

```
Service

↓

Room 101

Room 102

Room 103
```

---

Car rental

```
Service

↓

Toyota

BMW

Tesla
```

---

Coworking

```
Desk A

Desk B

Desk C
```

Without resources

your platform isn't really generic.

---

I'd create

```
resources

id

service_id

organization_id

name

type

capacity

status

metadata

created_at
```

Then

Booking

references

```
resource_id
```

instead of only

```
service_id
```

Now the booking engine becomes truly generic.

---

# My Final Recommendation

If this were my production architecture, I would stabilize the schemas in this order:

1. ✅ Auth (almost complete)
    
2. ✅ Audit (good foundation)
    
3. 🔥 Core (finish Booking Policy + introduce Resource)
    
4. 🔥 Tenant (plans, domains, settings, feature flags)
    
5. 🔥 Widget (credentials, origin validation, lifecycle)
    
6. Integration
    
7. Notification
    
8. Billing
    
9. Analytics
    
10. Webhook
    

The key architectural observation is that **Booking Policy should describe reservation behavior, not business types**. A hotel, barber, meeting room, sports court, or rental company should all be representable by the same reservation engine through combinations of policy configuration, resources, and capacity—not by branching business logic. That aligns closely with your long-term goal of making PleaseBookMe an API-first reservation infrastructure rather than an appointment application.