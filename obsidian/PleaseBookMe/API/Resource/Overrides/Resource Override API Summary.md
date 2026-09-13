# Overview

The Resource Override API allows application to create, retrieve, update, and delete one-off availability overrides for a resource — a window that deviates from its normal schedule.

## Base URL

```bash
/api/v1/resource-overrides
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
| POST | /api/v1/resource-overrides | [[Create a resource override]] |
| GET | /api/v1/resource-overrides/{resourceOverrideId} | [[Get a resource override]] |
| GET | /api/v1/resource-overrides | [[Get all resource overrides]] |
| PUT | /api/v1/resource-overrides/{resourceOverrideId} | [[Update a resource override]] |
| DELETE | /api/v1/resource-overrides/{resourceOverrideId} | [[Delete a resource override]] |
