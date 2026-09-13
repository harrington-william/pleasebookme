## Description

Retrieves every out-of-office record.

---

## Endpoint

```json
GET /api/v1/ooo
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
<https://api.pleasebookme.com/api/v1/ooo> \
-H "Authorization: Bearer xxx"
```
