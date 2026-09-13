# Overview

The Notification API allows application to create, retrieve, update, and delete notification records — the central row tying a recipient, a template, and a delivery channel together for a scheduled send.

## Base URL

```bash
/api/v1/notifications
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
| POST | /api/v1/notifications | [[Create a notification]] |
| GET | /api/v1/notifications/{notificationId} | [[Get a notification]] |
| GET | /api/v1/notifications | [[Get all notifications]] |
| PUT | /api/v1/notifications/{notificationId} | [[Update a notification]] |
| DELETE | /api/v1/notifications/{notificationId} | [[Delete a notification]] |
