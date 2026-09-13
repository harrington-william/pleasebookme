# Overview

The Notification Channel API allows application to create, retrieve, update, and delete delivery channels (email, SMS, push, in-app) that notifications can be sent through.

## Base URL

```bash
/api/v1/notification-channels
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
| POST | /api/v1/notification-channels | [[Create a notification channel]] |
| GET | /api/v1/notification-channels/{notificationChannelId} | [[Get a notification channel]] |
| GET | /api/v1/notification-channels | [[Get all notification channels]] |
| PUT | /api/v1/notification-channels/{notificationChannelId} | [[Update a notification channel]] |
| DELETE | /api/v1/notification-channels/{notificationChannelId} | [[Delete a notification channel]] |
