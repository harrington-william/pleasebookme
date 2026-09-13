# Overview

The Customer Activity API allows application to create, retrieve, update, and delete activity-log entries recorded against a customer

## Base URL

```bash
/api/v1/customer-activities
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
| POST | /api/v1/customer-activities | [[Create a customer activity]] |
| GET | /api/v1/customer-activities/{customerActivityId} | [[Get a customer activity]] |
| GET | /api/v1/customer-activities | [[Get all customer activities]] |
| PUT | /api/v1/customer-activities/{customerActivityId} | [[Update a customer activity]] |
| DELETE | /api/v1/customer-activities/{customerActivityId} | [[Delete a customer activity]] |
