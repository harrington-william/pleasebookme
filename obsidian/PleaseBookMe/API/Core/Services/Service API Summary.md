# Overview

The Service API allows application to create, retrieve, update, and delete a business's bookable services. A service is the busiest resource in the core booking domain — it ties together an owning user, profile, organization, and schedule, and optionally links out to a destination calendar/sheet for sync. Create/update derive `userId`, `profileId`, and `organizationId` from the authenticated caller instead of accepting ownership fields in the request body; create can also atomically persist a nested booking policy.

## Base URL

```bash
/api/v1/services
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
| POST | /api/v1/services | [[Create a service]] |
| GET | /api/v1/services?organizationId= | [[Get services by organization]] |
| GET | /api/v1/services/{serviceId} | [[Get a service]] |
| PUT | /api/v1/services/{serviceId} | [[Update a service]] |
| DELETE | /api/v1/services/{serviceId} | [[Delete a service]] |
