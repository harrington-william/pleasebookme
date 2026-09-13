# Overview

The Role API allows application to create, retrieve, update, and delete RBAC roles. A role is a named permission bundle (e.g. `PLATFORM_OWNER`, `STAFF`) assigned to users via `auth.user_roles` and granted permissions via `auth.role_permissions`.

## Base URL

```bash
/api/v1/roles
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
| POST | /api/v1/roles | [[Create a role]] |
| GET | /api/v1/roles/{roleId} | [[Get role by id]] |
| GET | /api/v1/roles | [[Get all roles]] |
| PUT | /api/v1/roles/{roleId} | [[Update a role]] |
| DELETE | /api/v1/roles/{roleId} | [[Delete a role]] |
