## Description

Returns one page of resources for the requested organization, optionally filtered.

`page` is zero-based and defaults to `0`; `size` defaults to `20` and is capped at `100`. A caller whose current organization does not match `organizationId` receives an empty page.

All filters are optional and combine with AND:

| Parameter | Type | Notes |
|---|---|---|
| `resourceTypeId` | integer | Exact match on the resource's type. |
| `status` | enum | One of `ACTIVE`, `INACTIVE`, `MAINTENANCE`, `RETIRED`. An unknown value is rejected with `400`. |
| `q` | string | Case-insensitive contains-match across `name` and `slug`. `%` and `_` are matched literally, not as wildcards. |
| `sort` | string | `field` or `field,asc` / `field,desc`. Sortable fields: `name`, `slug`, `capacity`, `status`, `createdAt`, `updatedAt`. Any other field is ignored rather than erroring, and the default is `createdAt,desc`. |

---

## Endpoint

```json
GET /api/v1/resources?organizationId={organizationId}&resourceTypeId={resourceTypeId}&status={status}&q={q}&sort={sort}&page={page}&size={size}
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
  "content": [
    {
      "resourceId": 12,
      "resourceUid": "4cbccb39-af20-43cb-aaf2-864c52dd12f7",
      "organizationId": 3,
      "resourceTypeId": 8,
      "name": "Conference Room A",
      "slug": "conference-room-a",
      "description": null,
      "capacity": null,
      "status": "ACTIVE",
      "isBookable": true,
      "isVirtual": false,
      "createdAt": "2026-09-01T00:00:00Z",
      "updatedAt": "2026-09-01T00:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
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
  "path": "/api/v1/resources"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/resources?organizationId=3&status=ACTIVE&q=room&sort=name,asc&page=0&size=20> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- None
