# Widget API Summary

# Overview

The Widget API manages organization-scoped dashboard widgets, one-time credential issuance, normalized embed origins, status counts, and soft revocation. Tenant ownership is derived from the caller; clients never submit a tenant ID. The former unscoped list-all capability no longer exists.

## Base URL

```bash
/api/v1/widgets
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
| POST | /api/v1/widgets/credentials | [[Generate widget credentials]] |
| POST | /api/v1/widgets | [[Create a widget]] |
| GET | /api/v1/widgets/stats?organizationId= | [[Get widget stats]] |
| GET | /api/v1/widgets?organizationId= | [[List widgets]] |
| GET | /api/v1/widgets/{widgetId} | [[Get a widget]] |
| PUT | /api/v1/widgets/{widgetId} | [[Update a widget]] |
| DELETE | /api/v1/widgets/{widgetId} | [[Delete a widget]] |
