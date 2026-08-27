# Overview

The Customer Source API allows application to create, retrieve, update, and delete acquisition-source records for a customer

## Base URL

```bash
/api/v1/customer-sources
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
| POST | /api/v1/customer-sources | [[Create a customer source]] |
| GET | /api/v1/customer-sources/{customerSourceId} | [[Get a customer source]] |
| GET | /api/v1/customer-sources | [[Get all customer sources]] |
| PUT | /api/v1/customer-sources/{customerSourceId} | [[Update a customer source]] |
| DELETE | /api/v1/customer-sources/{customerSourceId} | [[Delete a customer source]] |
