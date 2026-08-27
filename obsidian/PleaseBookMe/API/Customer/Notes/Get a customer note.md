## Description

Retrieves a single customer note by its numeric ID.

---

## Endpoint

```json
GET /api/v1/customer-notes/{customerNoteId}
```

---

## Authentication

Required **Bearer Token**

---

## Headers

```json
{
	"Authorization": "JWT Access Token"
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 CUSTOMER_NOTE_NOT_FOUND
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
-X GET \
<https://api.pleasebookme.com/api/v1/customer-notes/1> \
-H "Authorization: Bearer xxx"
```
