# Overview

The Resource Attribute API allows application to create, retrieve, update, and delete free-form key/value attributes attached to a resource. A row is uniquely identified by the pair `(resourceId, key)` — there is no surrogate ID.

## Base URL

```bash
/api/v1/resource-attributes
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
| POST | /api/v1/resource-attributes | [[Create a resource attribute]] |
| GET | /api/v1/resource-attributes/{resourceId}/{key} | [[Get a resource attribute]] |
| GET | /api/v1/resource-attributes | [[Get all resource attributes]] |
| PUT | /api/v1/resource-attributes/{resourceId}/{key} | [[Update a resource attribute]] |
| DELETE | /api/v1/resource-attributes/{resourceId}/{key} | [[Delete a resource attribute]] |
