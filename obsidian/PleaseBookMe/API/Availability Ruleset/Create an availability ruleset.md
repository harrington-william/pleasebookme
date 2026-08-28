## Description

Creates a schedule and one or more recurring availability windows for it in a single transaction — either every row commits, or none do. `timezone` falls back to the platform default (`Australia/Sydney`) when omitted. `windows` must contain at least one entry.

The request never accepts `userId`. Ownership of the schedule and every window is derived from the authenticated Bearer token, not the request body.

---

## Endpoint

```json
POST /api/v1/availability-rulesets
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
	"title": "",
	"timezone": "",
	"windows": [
		{
			"days": [],
			"startTime": "",
			"endTime": ""
		}
	]
}
```

## Successful Response

```json
201 Created
```

```json
{
	"schedule": {
		"scheduleId": 0,
		"userId": 0,
		"title": "",
		"timezone": "",
		"createdAt": "",
		"updatedAt": ""
	},
	"availabilities": [
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
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 USER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "USER_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/availability-rulesets> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
	"title": "Working Hours",
	"timezone": "Australia/Sydney",
	"windows": [
		{
			"days": [1, 2, 3, 4, 5],
			"startTime": "09:00:00",
			"endTime": "17:00:00"
		}
	]
}'
```

---

## Event Produced

- ScheduleCreated
- AvailabilityCreated
