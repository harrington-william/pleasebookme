# Overview

The Organization API allows application to create, retrieve, update, and delete organizations. An organization is a root entity with no foreign keys of its own — every other organization-scoped table (profiles, memberships) and several cross-schema tables (tenants, services, notifications, resources, audit events, customers) reference it.

## Base URL

```bash
/api/v1/organizations
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
| POST | /api/v1/organizations | [[Create an organization]] |
| GET | /api/v1/organizations/{organizationId} | [[Get an organization]] |
| GET | /api/v1/organizations | [[Get all organizations]] |
| PUT | /api/v1/organizations/{organizationId} | [[Update an organization]] |
| DELETE | /api/v1/organizations/{organizationId} | [[Delete an organization]] |
