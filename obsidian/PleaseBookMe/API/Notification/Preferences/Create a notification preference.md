## Description

Creates a per-user notification preference for a given `notificationType`. `(userId, notificationType)` must be unique. `emailEnabled`, `smsEnabled`, `pushEnabled`, and `inAppEnabled` all fall back to `false` when omitted.

---

## Endpoint

```json
POST /api/v1/notification-preferences
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
	"userId": 0,
	"notificationType": "",
	"emailEnabled": true,
	"smsEnabled": false,
	"pushEnabled": true,
	"inAppEnabled": true,
	"quietHoursStart": "22:00:00",
	"quietHoursEnd": "07:00:00"
}
```

## Successful Response

```json
201 Created
```

```json
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
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 USER_NOT_FOUND
- 409 NOTIFICATION_PREFERENCE_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "NOTIFICATION_PREFERENCE_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/notification-preferences> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "userId": 1,
	"notificationType": "BOOKING_CONFIRMED",
	"emailEnabled": true,
	"smsEnabled": false,
	"pushEnabled": true,
	"inAppEnabled": true,
	"quietHoursStart": "22:00:00",
	"quietHoursEnd": "07:00:00"
}'
```

---

## Event Produced

- NotificationPreferenceCreated
