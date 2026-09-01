## Description

Returns every resource-to-service assignment belonging to one organization, in a single call.

This exists so a resource list can render its assigned services without issuing one request per row. Prefer it over repeated `?resourceId=` calls whenever more than one resource is being displayed.

---

## Endpoint

```json
GET /api/v1/resource-services?organizationId={organizationId}
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
<https://api.pleasebookme.com/api/v1/resource-services?organizationId=3> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- None
