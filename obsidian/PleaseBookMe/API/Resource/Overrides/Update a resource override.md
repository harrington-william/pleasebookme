## Description

Full-replaces an existing resource override. `resourceId` is re-resolved on every update.

---

## Endpoint

```json
PUT /api/v1/resource-overrides/{resourceOverrideId}
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
	"reason": ""
}
```

## Successful Response

```json
200 OK
```

```json
{
	"resourceOverrideId": 0,
	"resourceId": 0,
	"startTime": "",
	"endTime": "",
	"reason": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_OVERRIDE_NOT_FOUND
- 404 RESOURCE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_OVERRIDE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/resource-overrides/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "resourceId": 1,
	"startTime": "2026-09-06T09:00:00Z",
	"endTime": "2026-09-06T20:00:00Z",
	"reason": "Reserved for a private event"
}'
```

---

## Event Produced

- ResourceOverrideUpdated
