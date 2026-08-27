## Description

Full-replaces an existing resource maintenance record. `resourceId` is re-resolved on every update.

---

## Endpoint

```json
PUT /api/v1/resource-maintenance/{resourceMaintenanceId}
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
200 OK
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
- 404 RESOURCE_MAINTENANCE_NOT_FOUND
- 404 RESOURCE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_MAINTENANCE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/resource-maintenance/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "resourceId": 1,
	"startTime": "2026-09-05T08:00:00Z",
	"endTime": "2026-09-05T14:00:00Z",
	"reason": "Deep cleaning",
	"status": "IN_PROGRESS"
}'
```

---

## Event Produced

- ResourceMaintenanceUpdated
