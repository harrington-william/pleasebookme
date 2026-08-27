## Description

Retrieves a single schedule by its numeric ID.

---

## Endpoint

```json
GET /api/v1/schedules/{scheduleId}
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
	"scheduleId": 0,
	"userId": 0,
	"title": "",
	"timezone": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 SCHEDULE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "SCHEDULE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/schedules/1> \
-H "Authorization: Bearer xxx"
```
