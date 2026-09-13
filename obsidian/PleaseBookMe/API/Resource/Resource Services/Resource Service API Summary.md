# Overview

The Resource Service API assigns a resource to one or more bookable services. Each assignment is identified by the composite pair `(resourceId, serviceId)` and has no update operation.

## Base URL

```bash
/api/v1/resource-services
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
Every endpoint in this resource requires a Bearer Token
```

---

# Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/v1/resource-services | [[Create a resource service]] |
| GET | /api/v1/resource-services/{resourceId}/{serviceId} | [[Get a resource service]] |
| GET | /api/v1/resource-services?resourceId= | [[Get resource services by resource]] |
| GET | /api/v1/resource-services?organizationId= | [[Get resource services by organization]] |
| DELETE | /api/v1/resource-services/{resourceId}/{serviceId} | [[Delete a resource service]] |
