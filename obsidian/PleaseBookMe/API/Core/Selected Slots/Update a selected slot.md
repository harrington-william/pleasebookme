## Description

Full-replaces an existing selected slot. `isSeat` is only overwritten when supplied; a missing value keeps its current stored value. The composite `(serviceId, userId, slotStart, slotEnd)` uniqueness check performed on create is **not** re-checked on update.

---

## Endpoint

```json
PUT /api/v1/selected-slots/{selectedSlotId}
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
	"serviceId": 0,
	"userId": 0,
	"slotStart": "",
	"slotEnd": "",
	"releaseAt": "",
	"isSeat": false
}
```

## Successful Response

```json
200 OK
```

```json
{
	"selectedSlotId": 0,
	"selectedSlotUid": "",
	"serviceId": 0,
	"userId": 0,
	"slotStart": "",
	"slotEnd": "",
	"releaseAt": "",
	"isSeat": false,
	"createdAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 SELECTED_SLOT_NOT_FOUND
- 404 SERVICE_NOT_FOUND
- 404 USER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "SELECTED_SLOT_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/selected-slots/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "serviceId": 1,
	"userId": 1,
	"slotStart": "2026-08-25T09:00:00Z",
	"slotEnd": "2026-08-25T09:30:00Z",
	"releaseAt": "2026-08-25T09:15:00Z",
	"isSeat": false
}'
```

---

## Event Produced

- SelectedSlotUpdated
