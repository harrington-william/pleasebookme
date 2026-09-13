## Description

Marks `userId` as out of office for the `[startTime, endTime]` range, with bookings optionally redirected to `toUserId`. `showNotePublicly` defaults to `false` when omitted.

---

## Endpoint

```json
POST /api/v1/ooo
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
	"startTime": "",
	"endTime": "",
	"notes": "",
	"showNotePublicly": false,
	"userId": 0,
	"toUserId": 0,
	"reason": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"outOfOfficeId": 0,
	"outOfOfficeUid": "",
	"startTime": "",
	"endTime": "",
	"notes": "",
	"showNotePublicly": false,
	"userId": 0,
	"toUserId": 0,
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
- 404 USER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "USER_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/ooo> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "startTime": "2026-09-01T00:00:00Z",
	"endTime": "2026-09-05T00:00:00Z",
	"notes": "On annual leave",
	"showNotePublicly": true,
	"userId": 1,
	"toUserId": 2,
	"reason": "Vacation"
}'
```

---

## Event Produced

- OutOfOfficeCreated
