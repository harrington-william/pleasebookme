## Description

Full-replaces an existing out-of-office record — the caller is expected to send the complete resource. `userId`/`toUserId` are re-resolved on every call, so either may 404 independently. `showNotePublicly` is only overwritten when supplied.

---

## Endpoint

```json
PUT /api/v1/ooo/{outOfOfficeId}
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
200 OK
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
- 404 OUT_OF_OFFICE_NOT_FOUND
- 404 USER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "OUT_OF_OFFICE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/ooo/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "startTime": "2026-09-01T00:00:00Z",
	"endTime": "2026-09-07T00:00:00Z",
	"notes": "Extended by two days",
	"showNotePublicly": true,
	"userId": 1,
	"toUserId": 2,
	"reason": "Vacation"
}'
```

---

## Event Produced

- OutOfOfficeUpdated
