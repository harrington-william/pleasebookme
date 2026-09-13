# Overview

The User Role API allows application to assign, retrieve, and revoke role assignments between users and roles. This is a pure composite-key join resource — `userId` + `roleId` together are the primary key, so there is no update endpoint (nothing on the row is mutable beyond the immutable assignment timestamp).

## Base URL

```bash
/api/v1/user-roles
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
| POST | /api/v1/user-roles | [[Create a user role]] |
| GET | /api/v1/user-roles/{userId}/{roleId} | [[Get a user role]] |
| GET | /api/v1/user-roles | [[Get all user roles]] |
| DELETE | /api/v1/user-roles/{userId}/{roleId} | [[Delete a user role]] |
