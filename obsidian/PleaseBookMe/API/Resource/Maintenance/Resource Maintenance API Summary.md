# Overview

The Resource Maintenance API allows application to create, retrieve, update, and delete maintenance windows for a resource — periods during which it is unavailable for booking.

## Base URL

```bash
/api/v1/resource-maintenance
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
| POST | /api/v1/resource-maintenance | [[Create a resource maintenance record]] |
| GET | /api/v1/resource-maintenance/{resourceMaintenanceId} | [[Get a resource maintenance record]] |
| GET | /api/v1/resource-maintenance | [[Get all resource maintenance records]] |
| PUT | /api/v1/resource-maintenance/{resourceMaintenanceId} | [[Update a resource maintenance record]] |
| DELETE | /api/v1/resource-maintenance/{resourceMaintenanceId} | [[Delete a resource maintenance record]] |
