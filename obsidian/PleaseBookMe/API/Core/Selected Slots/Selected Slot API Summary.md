# Overview

The Selected Slot API allows application to create, retrieve, update, and delete a temporary slot hold — the reservation a booker places on a service's time slot while completing checkout, before it either expires (`releaseAt`) or converts into a confirmed booking

## Base URL

```bash
/api/v1/selected-slots
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
| POST | /api/v1/selected-slots | [[Create a selected slot]] |
| GET | /api/v1/selected-slots/{selectedSlotId} | [[Get a selected slot]] |
| GET | /api/v1/selected-slots | [[Get all selected slots]] |
| PUT | /api/v1/selected-slots/{selectedSlotId} | [[Update a selected slot]] |
| DELETE | /api/v1/selected-slots/{selectedSlotId} | [[Delete a selected slot]] |
