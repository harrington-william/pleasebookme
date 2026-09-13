## Description

Retrieves every customer note.

---

## Endpoint

```json
GET /api/v1/customer-notes
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
[
	{
		"customerNoteId": 0,
		"customerId": 0,
		"authorUserId": 0,
		"content": "",
		"createdAt": ""
	}
]
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "UNAUTHORIZED"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/customer-notes> \
-H "Authorization: Bearer xxx"
```
