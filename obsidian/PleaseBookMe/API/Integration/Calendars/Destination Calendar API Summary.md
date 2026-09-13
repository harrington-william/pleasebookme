# Overview

The Destination Calendar API allows application to create, retrieve, update, and delete the link between a service and an external calendar (e.g. Google Calendar) that its bookings sync to.

## Base URL

```bash
/api/v1/destination-calendars
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
| POST | /api/v1/destination-calendars | [[Create a destination calendar]] |
| GET | /api/v1/destination-calendars/{destinationCalendarId} | [[Get a destination calendar]] |
| GET | /api/v1/destination-calendars | [[Get all destination calendars]] |
| PUT | /api/v1/destination-calendars/{destinationCalendarId} | [[Update a destination calendar]] |
| DELETE | /api/v1/destination-calendars/{destinationCalendarId} | [[Delete a destination calendar]] |
