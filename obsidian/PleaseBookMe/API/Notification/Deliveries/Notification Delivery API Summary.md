# Overview

The Notification Delivery API allows application to create, retrieve, update, and delete delivery-attempt records for a notification against a specific external provider (e.g. an email/SMS gateway).

## Base URL

```bash
/api/v1/notification-deliveries
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
| POST | /api/v1/notification-deliveries | [[Create a notification delivery]] |
| GET | /api/v1/notification-deliveries/{notificationDeliveryId} | [[Get a notification delivery]] |
| GET | /api/v1/notification-deliveries | [[Get all notification deliveries]] |
| PUT | /api/v1/notification-deliveries/{notificationDeliveryId} | [[Update a notification delivery]] |
| DELETE | /api/v1/notification-deliveries/{notificationDeliveryId} | [[Delete a notification delivery]] |
