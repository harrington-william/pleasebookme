# Overview

The Resource API allows application to create, retrieve, update, and delete resources — the bookable unit (a chair, a room, a court, a piece of equipment) that a service allocates for a reservation.

## Base URL

```bash
/api/v1/resources
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
| POST | /api/v1/resources | [[Create a resource]] |
| GET | /api/v1/resources/{resourceId} | [[Get a resource]] |
| GET | /api/v1/resources | [[Get all resources]] |
| PUT | /api/v1/resources/{resourceId} | [[Update a resource]] |
| DELETE | /api/v1/resources/{resourceId} | [[Delete a resource]] |
