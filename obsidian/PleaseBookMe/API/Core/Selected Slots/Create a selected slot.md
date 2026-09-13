## Description

Holds a `(service, user, slotStart, slotEnd)` slot combination so it can't be double-booked while a booker completes checkout. `isSeat` falls back to `false` when omitted. The same `(serviceId, userId, slotStart, slotEnd)` combination cannot be held twice — a second attempt is rejected as a conflict rather than silently accepted.

---

## Endpoint

```json
POST /api/v1/selected-slots
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
201 Created
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
- 404 SERVICE_NOT_FOUND
- 404 USER_NOT_FOUND
- 409 SELECTED_SLOT_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "SELECTED_SLOT_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/selected-slots> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "serviceId": 1,
	"userId": 1,
	"slotStart": "2026-08-25T09:00:00Z",
	"slotEnd": "2026-08-25T09:30:00Z",
	"releaseAt": "2026-08-25T09:10:00Z",
	"isSeat": false
}'
```

---

## Event Produced

- SelectedSlotCreated
