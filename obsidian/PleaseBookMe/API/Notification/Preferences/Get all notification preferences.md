## Description

Retrieves every notification preference.

---

## Endpoint

```json
GET /api/v1/notification-preferences
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
		"notificationPreferenceId": 0,
		"userId": 0,
		"notificationType": "",
		"emailEnabled": true,
		"smsEnabled": false,
		"pushEnabled": true,
		"inAppEnabled": true,
		"quietHoursStart": "22:00:00",
		"quietHoursEnd": "07:00:00",
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
<https://api.pleasebookme.com/api/v1/notification-preferences> \
-H "Authorization: Bearer xxx"
```
