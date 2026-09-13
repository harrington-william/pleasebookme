# Overview

The Permission API allows application to create, retrieve, update, and delete permissions. A permission is a single RBAC capability, addressed by a unique `slug` in the `RESOURCE.ACTION` format (e.g. `USER.CREATE`), and is assigned to roles via the Role Permission API.

## Base URL

```bash
/api/v1/permissions
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
| POST | /api/v1/permissions | [[Create a permission]] |
| GET | /api/v1/permissions/{permissionId} | [[Get a permission]] |
| GET | /api/v1/permissions | [[Get all permissions]] |
| PUT | /api/v1/permissions/{permissionId} | [[Update a permission]] |
| DELETE | /api/v1/permissions/{permissionId} | [[Delete a permission]] |
