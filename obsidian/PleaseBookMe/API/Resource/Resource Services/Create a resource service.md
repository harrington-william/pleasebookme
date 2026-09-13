## Description

Assigns an existing resource to an existing service. The pair is unique; creating the same assignment twice returns `409 Conflict` rather than silently merging it.

---

## Endpoint

```json
POST /api/v1/resource-services
```

---

## Authentication

Required **Bearer Token**

---

## Headers

```json
{
  "Authorization": "JWT Access Token",
  "Content-Type": "application/json"
}
```

## Body

```json
{
  "resourceId": 12,
  "serviceId": 5
}
```

## Success Response

```json
201 Created
```

```json
{
  "resourceId": 12,
  "serviceId": 5,
  "assignedAt": "2026-09-01T00:00:00Z"
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_NOT_FOUND
- 404 SERVICE_NOT_FOUND
- 409 RESOURCE_SERVICE_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
  "status": "error",
  "message": "Service 5 is already assigned to resource 12",
  "data": null,
  "client": "127.0.0.1",
  "timestamp": "2026-09-01T00:00:00Z",
  "path": "/api/v1/resource-services"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/resource-services> \
-H "Authorization: Bearer xxx" \
-H "Content-Type: application/json" \
-d '{"resourceId":12,"serviceId":5}'
```

---

## Event Produced

- None
