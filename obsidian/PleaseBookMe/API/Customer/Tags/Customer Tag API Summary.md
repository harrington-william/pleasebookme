# Overview

The Customer Tag API allows application to create, retrieve, update, and delete tags attached to a customer

## Base URL

```bash
/api/v1/customer-tags
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
| POST | /api/v1/customer-tags | [[Create a customer tag]] |
| GET | /api/v1/customer-tags/{customerTagId} | [[Get a customer tag]] |
| GET | /api/v1/customer-tags | [[Get all customer tags]] |
| PUT | /api/v1/customer-tags/{customerTagId} | [[Update a customer tag]] |
| DELETE | /api/v1/customer-tags/{customerTagId} | [[Delete a customer tag]] |
