## Description

Retrieves one resource-service assignment by both parts of its composite identifier.

---

## Endpoint

```json
GET /api/v1/resource-services/{resourceId}/{serviceId}
```

---

## Authentication

Required **Bearer Token**

---

## Headers

```json
{
  "Authorization": "JWT Access Token"
}
```

## Body

No request body.

## Success Response

```json
200 OK
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_SERVICE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
  "status": "error",
  "message": "Service 5 is not assigned to resource 12",
  "data": null,
  "client": "127.0.0.1",
  "timestamp": "2026-09-01T00:00:00Z",
  "path": "/api/v1/resource-services/12/5"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/resource-services/12/5> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- None
