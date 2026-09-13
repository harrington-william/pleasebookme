# Overview

The Widget Origin API allows application to create, retrieve, update, and delete the allowed origin domains registered against a widget, used to validate that embedding requests come from an authorized website.

## Base URL

```bash
/api/v1/widget-origins
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
| POST | /api/v1/widget-origins | [[Create a widget origin]] |
| GET | /api/v1/widget-origins/{widgetOriginId} | [[Get a widget origin]] |
| GET | /api/v1/widget-origins | [[Get all widget origins]] |
| PUT | /api/v1/widget-origins/{widgetOriginId} | [[Update a widget origin]] |
| DELETE | /api/v1/widget-origins/{widgetOriginId} | [[Delete a widget origin]] |
