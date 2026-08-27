# Overview

The Schedule API allows application to create, retrieve, update, and delete a user's schedules — the container that a schedule's availability windows attach to.

## Base URL

```bash
/api/v1/schedules
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
| POST | /api/v1/schedules | [[Create a schedule]] |
| GET | /api/v1/schedules/{scheduleId} | [[Get a schedule]] |
| GET | /api/v1/schedules | [[Get all schedules]] |
| PUT | /api/v1/schedules/{scheduleId} | [[Update a schedule]] |
| DELETE | /api/v1/schedules/{scheduleId} | [[Delete a schedule]] |
