# Overview

The Out of Office API allows application to create, retrieve, update, and delete out-of-office records, which mark a user as unavailable for a time range and optionally redirect their bookings to another user

## Base URL

```bash
/api/v1/ooo
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
| POST | /api/v1/ooo | [[Create an out of office record]] |
| GET | /api/v1/ooo/{outOfOfficeId} | [[Get an out of office record]] |
| GET | /api/v1/ooo | [[Get all out of office records]] |
| PUT | /api/v1/ooo/{outOfOfficeId} | [[Update an out of office record]] |
| DELETE | /api/v1/ooo/{outOfOfficeId} | [[Delete an out of office record]] |
