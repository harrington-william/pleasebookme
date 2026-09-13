# Overview

The Ecosystem API allows application to create, retrieve, update, and delete ecosystems — the business-category lookup values (e.g. barbershop, rental, court booking) that tenants classify under.

## Base URL

```bash
/api/v1/ecosystems
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
| POST | /api/v1/ecosystems | [[Create an ecosystem]] |
| GET | /api/v1/ecosystems/{ecosystemId} | [[Get an ecosystem]] |
| GET | /api/v1/ecosystems | [[Get all ecosystems]] |
| PUT | /api/v1/ecosystems/{ecosystemId} | [[Update an ecosystem]] |
| DELETE | /api/v1/ecosystems/{ecosystemId} | [[Delete an ecosystem]] |
