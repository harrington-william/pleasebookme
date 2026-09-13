# Overview

The Membership Role API allows application to assign, retrieve, and revoke role assignments between memberships and roles. This is a pure composite-key join resource — `membershipId` + `roleId` together are the primary key, so there is no update endpoint (nothing on the row is mutable beyond the immutable assignment timestamp).

## Base URL

```bash
/api/v1/membership-roles
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
| POST | /api/v1/membership-roles | [[Create a membership role]] |
| GET | /api/v1/membership-roles/{membershipId}/{roleId} | [[Get a membership role]] |
| GET | /api/v1/membership-roles | [[Get all membership roles]] |
| DELETE | /api/v1/membership-roles/{membershipId}/{roleId} | [[Delete a membership role]] |
