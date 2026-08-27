# Overview

The Notification Queue API allows application to create, retrieve, update, and delete queue entries that drive asynchronous notification delivery attempts.

## Base URL

```bash
/api/v1/notification-queue
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
| POST | /api/v1/notification-queue | [[Create a notification queue entry]] |
| GET | /api/v1/notification-queue/{notificationQueueId} | [[Get a notification queue entry]] |
| GET | /api/v1/notification-queue | [[Get all notification queue entries]] |
| PUT | /api/v1/notification-queue/{notificationQueueId} | [[Update a notification queue entry]] |
| DELETE | /api/v1/notification-queue/{notificationQueueId} | [[Delete a notification queue entry]] |
