# Overview

The Booking Policy API allows application to create, retrieve, update, and delete a service's booking rules — duration, notice, advance-booking window, buffers, overlap, capacity, and confirmation behavior. `serviceId` is unique at the database level and checked before create — a service has at most one policy, with duplicate creates returning `409`.

## Base URL

```bash
/api/v1/booking-policies
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
| POST | /api/v1/booking-policies | [[Create a booking policy]] |
| GET | /api/v1/booking-policies/{bookingPolicyId} | [[Get a booking policy]] |
| GET | /api/v1/booking-policies | [[Get all booking policies]] |
| PUT | /api/v1/booking-policies/{bookingPolicyId} | [[Update a booking policy]] |
| DELETE | /api/v1/booking-policies/{bookingPolicyId} | [[Delete a booking policy]] |
