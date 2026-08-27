# Overview

The Widget API allows application to create, retrieve, update, and delete embeddable booking widgets registered against a tenant. `secretKey` is a bearer credential — it is required to create/update a widget but never echoed back in a response.

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
| POST | /api/v1/widgets | [[Create a widget]] |
| GET | /api/v1/widgets/{widgetId} | [[Get a widget]] |
| GET | /api/v1/widgets | [[Get all widgets]] |
| PUT | /api/v1/widgets/{widgetId} | [[Update a widget]] |
| DELETE | /api/v1/widgets/{widgetId} | [[Delete a widget]] |
