## Description

Creates a maintenance window for a resource. `status` is a free-form string (not a fixed enum) — the caller decides its vocabulary (e.g. `"SCHEDULED"`, `"IN_PROGRESS"`, `"COMPLETED"`). There is no uniqueness constraint — overlapping windows are not rejected by the API.

---

## Endpoint

```json
POST /api/v1/resource-maintenance
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
	"resourceId": 0,
	"startTime": "",
	"endTime": "",
	"reason": "",
	"status": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"resourceMaintenanceId": 0,
	"resourceId": 0,
	"startTime": "",
	"endTime": "",
	"reason": "",
	"status": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/resource-maintenance> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "resourceId": 1,
	"startTime": "2026-09-05T08:00:00Z",
	"endTime": "2026-09-05T12:00:00Z",
	"reason": "Deep cleaning",
	"status": "SCHEDULED"
}'
```

---

## Event Produced

- ResourceMaintenanceCreated
