# Overview

The Resource Type API allows application to create, retrieve, update, and delete resource types — the reusable category (e.g. "Barber Chair", "Meeting Room") an organization groups its bookable resources under.

## Base URL

```bash
/api/v1/resource-types
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
| POST | /api/v1/resource-types | [[Create a resource type]] |
| GET | /api/v1/resource-types/{resourceTypeId} | [[Get a resource type]] |
| GET | /api/v1/resource-types | [[Get all resource types]] |
| PUT | /api/v1/resource-types/{resourceTypeId} | [[Update a resource type]] |
| DELETE | /api/v1/resource-types/{resourceTypeId} | [[Delete a resource type]] |
