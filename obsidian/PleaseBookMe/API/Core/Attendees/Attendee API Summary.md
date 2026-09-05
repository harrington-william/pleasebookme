# Overview

The Attendee API allows application to create, retrieve, update, and delete attendees on a booking

An attendee is the per-booking contact snapshot taken at reservation time, deliberately separate from the durable `customer.customers` CRM record — there is no foreign key between them. Listing is scoped: `GET /api/v1/attendees` requires either a `bookingId` or an `organizationId`, and there is no endpoint that returns every attendee on the platform.

## Base URL

```bash
/api/v1/attendees
```

## Protocol

```bash
HTTPS
```

## Response Format

```bash
application/json
```

## Character Encoding

```bash
UTF-8
```

## Authentication

```bash
Every endpoint in this resource requires a **Bearer Token**
```

---

# Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/v1/attendees | [[Create an attendee]] |
| GET | /api/v1/attendees/{attendeeId} | [[Get an attendee]] |
| GET | /api/v1/attendees | [[List attendees]] |
| PUT | /api/v1/attendees/{attendeeId} | [[Update an attendee]] |
| DELETE | /api/v1/attendees/{attendeeId} | [[Delete an attendee]] |
