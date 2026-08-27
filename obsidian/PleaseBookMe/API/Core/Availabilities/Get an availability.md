## Description

Retrieves a single availability window by its numeric ID.

---

## Endpoint

```json
GET /api/v1/availabilities/{availabilityId}
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
	"availabilityId": 0,
	"userId": 0,
	"scheduleId": 0,
	"days": [],
	"startTime": "",
	"endTime": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 AVAILABILITY_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "AVAILABILITY_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/availabilities/1> \
-H "Authorization: Bearer xxx"
```
