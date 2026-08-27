# Overview

The Resource Pricing API allows application to create, retrieve, update, and delete time-bounded price records for a resource.

## Base URL

```bash
/api/v1/resource-pricing
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
| POST | /api/v1/resource-pricing | [[Create a resource pricing record]] |
| GET | /api/v1/resource-pricing/{resourcePricingId} | [[Get a resource pricing record]] |
| GET | /api/v1/resource-pricing | [[Get all resource pricing records]] |
| PUT | /api/v1/resource-pricing/{resourcePricingId} | [[Update a resource pricing record]] |
| DELETE | /api/v1/resource-pricing/{resourcePricingId} | [[Delete a resource pricing record]] |
