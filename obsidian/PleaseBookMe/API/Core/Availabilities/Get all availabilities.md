## Description

Retrieves every availability window.

---

## Endpoint

```json
GET /api/v1/availabilities
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
		"availabilityId": 0,
		"userId": 0,
		"scheduleId": 0,
		"days": [],
		"startTime": "",
		"endTime": "",
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
<https://api.pleasebookme.com/api/v1/availabilities> \
-H "Authorization: Bearer xxx"
```
