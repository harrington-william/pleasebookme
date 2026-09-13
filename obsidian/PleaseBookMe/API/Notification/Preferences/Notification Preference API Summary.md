# Overview

The Notification Preference API allows application to create, retrieve, update, and delete a user's per-notification-type delivery preferences (email/SMS/push/in-app opt-in and quiet hours).

## Base URL

```bash
/api/v1/notification-preferences
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
| POST | /api/v1/notification-preferences | [[Create a notification preference]] |
| GET | /api/v1/notification-preferences/{notificationPreferenceId} | [[Get a notification preference]] |
| GET | /api/v1/notification-preferences | [[Get all notification preferences]] |
| PUT | /api/v1/notification-preferences/{notificationPreferenceId} | [[Update a notification preference]] |
| DELETE | /api/v1/notification-preferences/{notificationPreferenceId} | [[Delete a notification preference]] |
