# Overview

The Role Permission API allows application to assign, retrieve, and revoke a permission's assignment to a role. `role_permissions` is a pure composite-key join table (`roleId` + `permissionId`) with no other mutable columns, so there is no update endpoint — only create (assign), read, and delete (revoke).

## Base URL

```bash
/api/v1/role-permissions
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
| POST | /api/v1/role-permissions | [[Create a role permission]] |
| GET | /api/v1/role-permissions/{roleId}/{permissionId} | [[Get a role permission]] |
| GET | /api/v1/role-permissions | [[Get all role permissions]] |
| DELETE | /api/v1/role-permissions/{roleId}/{permissionId} | [[Delete a role permission]] |
