## Description

Removes one resource-service assignment. The resource and service rows remain unchanged.

---

## Endpoint

```json
DELETE /api/v1/resource-services/{resourceId}/{serviceId}
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
204 No Content
```

No response body.

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
-X DELETE \
<https://api.pleasebookme.com/api/v1/resource-services/12/5> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- None
