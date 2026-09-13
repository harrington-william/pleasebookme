# Overview

The Membership API allows application to create, retrieve, update, and delete memberships linking a user to an organization. A membership tracks whether a user has accepted access to an organization — it is distinct from a profile, which carries the user's display identity within that organization.

## Base URL

```bash
/api/v1/memberships
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
| POST | /api/v1/memberships | [[Create a membership]] |
| GET | /api/v1/memberships/{membershipId} | [[Get a membership]] |
| GET | /api/v1/memberships | [[Get all memberships]] |
| PUT | /api/v1/memberships/{membershipId} | [[Update a membership]] |
| DELETE | /api/v1/memberships/{membershipId} | [[Delete a membership]] |
