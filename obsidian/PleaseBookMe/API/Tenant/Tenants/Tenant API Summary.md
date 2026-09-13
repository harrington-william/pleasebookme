# Overview

The Tenant API allows application to create, retrieve, update, and delete tenants — the active-subscription record (plan, ecosystem, region, quotas) representing an organization that has upgraded from a free self-serve account into a paying workspace.

## Base URL

```bash
/api/v1/tenants
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
| POST | /api/v1/tenants | [[Create a tenant]] |
| GET | /api/v1/tenants/{tenantId} | [[Get a tenant]] |
| GET | /api/v1/tenants | [[Get all tenants]] |
| PUT | /api/v1/tenants/{tenantId} | [[Update a tenant]] |
| DELETE | /api/v1/tenants/{tenantId} | [[Delete a tenant]] |
