## Description

Creates a one-off availability override for a resource. `reason` is required free text and has no length limit. There is no uniqueness constraint — overlapping overrides are not rejected by the API.

---

## Endpoint

```json
POST /api/v1/resource-overrides
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
201 Created
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
<https://api.pleasebookme.com/api/v1/resource-overrides> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "resourceId": 1,
	"startTime": "2026-09-06T09:00:00Z",
	"endTime": "2026-09-06T17:00:00Z",
	"reason": "Reserved for a private event"
}'
```

---

## Event Produced

- ResourceOverrideCreated
