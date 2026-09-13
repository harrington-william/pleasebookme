# Overview

The Notification Template API allows application to create, retrieve, update, and delete message templates used to render notification subject/body content. A template is either platform-wide (`tenantId` omitted) or scoped to a single tenant.

## Base URL

```bash
/api/v1/notification-templates
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
| POST | /api/v1/notification-templates | [[Create a notification template]] |
| GET | /api/v1/notification-templates/{notificationTemplateId} | [[Get a notification template]] |
| GET | /api/v1/notification-templates | [[Get all notification templates]] |
| PUT | /api/v1/notification-templates/{notificationTemplateId} | [[Update a notification template]] |
| DELETE | /api/v1/notification-templates/{notificationTemplateId} | [[Delete a notification template]] |
