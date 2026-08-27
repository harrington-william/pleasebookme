# Overview

The Booking API allows application to create, retrieve, update, and delete bookings — a reservation of a service for a specific user across a start/end time window

## Base URL

```bash
/api/v1/bookings
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
| POST | /api/v1/bookings | [[Create a booking]] |
| GET | /api/v1/bookings/{bookingId} | [[Get a booking]] |
| GET | /api/v1/bookings | [[Get all bookings]] |
| PUT | /api/v1/bookings/{bookingId} | [[Update a booking]] |
| DELETE | /api/v1/bookings/{bookingId} | [[Delete a booking]] |
