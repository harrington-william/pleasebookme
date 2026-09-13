# Overview

The Availability API allows application to create, retrieve, update, and delete a schedule's recurring availability windows — the days of the week and time range a schedule is open for bookings.

## Base URL

```bash
/api/v1/availabilities
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
| POST | /api/v1/availabilities | [[Create an availability]] |
| GET | /api/v1/availabilities/{availabilityId} | [[Get an availability]] |
| GET | /api/v1/availabilities | [[Get all availabilities]] |
| PUT | /api/v1/availabilities/{availabilityId} | [[Update an availability]] |
| DELETE | /api/v1/availabilities/{availabilityId} | [[Delete an availability]] |
