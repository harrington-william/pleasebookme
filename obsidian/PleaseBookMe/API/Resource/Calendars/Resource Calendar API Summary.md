# Overview

The Resource Calendar API allows application to create, retrieve, update, and delete links between a resource and a schedule, so a resource's availability follows a defined schedule.

## Base URL

```bash
/api/v1/resource-calendars
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
| POST | /api/v1/resource-calendars | [[Create a resource calendar]] |
| GET | /api/v1/resource-calendars/{resourceCalendarId} | [[Get a resource calendar]] |
| GET | /api/v1/resource-calendars | [[Get all resource calendars]] |
| PUT | /api/v1/resource-calendars/{resourceCalendarId} | [[Update a resource calendar]] |
| DELETE | /api/v1/resource-calendars/{resourceCalendarId} | [[Delete a resource calendar]] |
