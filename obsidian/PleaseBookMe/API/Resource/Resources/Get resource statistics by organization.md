## Description

Returns resource counts for the requested organization, used by the dashboard tiles. A caller whose current organization does not match `organizationId` receives all-zero counts rather than an error, so the endpoint cannot be used to probe other organizations.

Utilization is deliberately **not** part of this response: it requires booking data joined against resource availability, and neither the query nor an agreed definition exists yet.

---

## Endpoint

```json
GET /api/v1/resources/stats?organizationId={organizationId}
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
  "total": 142,
  "active": 128,
  "inactive": 6,
  "maintenance": 14,
  "retired": 2
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
  "path": "/api/v1/resources/stats"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/resources/stats?organizationId=3> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- None
