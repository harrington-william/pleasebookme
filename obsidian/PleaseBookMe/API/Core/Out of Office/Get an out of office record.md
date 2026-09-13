## Description

Retrieves a single out-of-office record by its numeric ID.

---

## Endpoint

```json
GET /api/v1/ooo/{outOfOfficeId}
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

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 OUT_OF_OFFICE_NOT_FOUND
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
-X GET \
<https://api.pleasebookme.com/api/v1/ooo/1> \
-H "Authorization: Bearer xxx"
```
