## Description

Adds an internal note to a customer. `authorUserId` is optional — omit it to record a note with no attributed author.

---

## Endpoint

```json
POST /api/v1/customer-notes
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
201 Created
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
- 404 CUSTOMER_NOT_FOUND
- 404 USER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "CUSTOMER_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/customer-notes> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "customerId": 1,
	"authorUserId": 1,
	"content": "Prefers appointments in the morning."
}'
```

---

## Event Produced

- CustomerNoteCreated
