## Description

Returns every service assignment for one resource. The response contains identifiers only; clients resolve service display names through `GET /api/v1/services?organizationId=`.

---

## Endpoint

```json
GET /api/v1/resource-services?resourceId={resourceId}
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
[
  {
    "resourceId": 12,
    "serviceId": 5,
    "assignedAt": "2026-09-01T00:00:00Z"
  }
]
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
  "status": "error",
  "message": "Session expired.",
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
-X GET \
<https://api.pleasebookme.com/api/v1/resource-services?resourceId=12> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- None
