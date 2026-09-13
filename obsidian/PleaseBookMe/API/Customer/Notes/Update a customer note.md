## Description

Full-replace semantics — the caller is expected to send the complete resource. `customerId` and `authorUserId` are both re-resolved on every call; `authorUserId` can be cleared by omitting it.

---

## Endpoint

```json
PUT /api/v1/customer-notes/{customerNoteId}
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
	"customerId": 0,
	"authorUserId": 0,
	"content": ""
}
```

## Successful Response

```json
200 OK
```

```json
{
	"customerNoteId": 0,
	"customerId": 0,
	"authorUserId": 0,
	"content": "",
	"createdAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 CUSTOMER_NOTE_NOT_FOUND
- 404 CUSTOMER_NOT_FOUND
- 404 USER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "CUSTOMER_NOTE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/customer-notes/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "customerId": 1,
	"authorUserId": 1,
	"content": "Prefers appointments in the afternoon."
}'
```

---

## Event Produced

- CustomerNoteUpdated
