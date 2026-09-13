# Overview

The Resource Assignment API allows application to create, retrieve, update, and delete assignments of an organization membership to a resource — who is assigned to operate or use a given resource, and for how long.

## Base URL

```bash
/api/v1/resource-assignments
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
| POST | /api/v1/resource-assignments | [[Create a resource assignment]] |
| GET | /api/v1/resource-assignments/{resourceAssignmentId} | [[Get a resource assignment]] |
| GET | /api/v1/resource-assignments | [[Get all resource assignments]] |
| PUT | /api/v1/resource-assignments/{resourceAssignmentId} | [[Update a resource assignment]] |
| DELETE | /api/v1/resource-assignments/{resourceAssignmentId} | [[Delete a resource assignment]] |
