# Overview

The Profile API allows application to create, retrieve, update, and delete per-organization user profiles. A profile is distinct from a membership — it carries the display identity (`username`) a user has within one specific organization, while a membership just tracks organization access/acceptance.

## Base URL

```bash
/api/v1/profiles
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
| POST | /api/v1/profiles | [[Create a profile]] |
| GET | /api/v1/profiles/{profileId} | [[Get a profile]] |
| GET | /api/v1/profiles | [[Get all profiles]] |
| PUT | /api/v1/profiles/{profileId} | [[Update a profile]] |
| DELETE | /api/v1/profiles/{profileId} | [[Delete a profile]] |
