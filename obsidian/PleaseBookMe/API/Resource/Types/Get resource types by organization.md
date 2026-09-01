## Description

Returns all resource types belonging to the requested organization. This endpoint is unpaginated because it supplies a small reference list for resource creation forms. A caller whose current organization does not match `organizationId` receives an empty array.

---

## Endpoint

```json
GET /api/v1/resource-types?organizationId={organizationId}
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
    "resourceTypeId": 8,
    "organizationId": 3,
    "name": "Meeting Room",
    "description": "Shared meeting spaces",
    "icon": null,
    "createdAt": "2026-09-01T00:00:00Z",
    "updatedAt": "2026-09-01T00:00:00Z"
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
  "path": "/api/v1/resource-types"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/resource-types?organizationId=3> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- None
