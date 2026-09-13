# Overview

The Slot API returns the bookable time slots for one service on one calendar date, computed at request time from the service's availability windows, its booking policy, and everything already occupying the host — bookings and holds against *any* of the host's services, plus the host's out-of-office periods. It is the read side of the reservation engine and the first endpoint built for the booking widget.

It only offers slots. Holding or booking one is a separate call to the Selected Slots and Bookings APIs, and neither of those re-checks this engine today.

Any authenticated actor — a dashboard user or a bootstrapped widget — may query any service. No tenant or service scoping is applied yet; this is a recorded decision pending the cross-domain authorization plan, and the response never reveals *why* a slot is missing.

## Base URL

```bash
/api/v1/slots
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
Every endpoint in this resource requires a **Bearer Token** (user or widget)
```

---

# Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/v1/slots | [[Get available slots]] |
