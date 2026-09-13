## Description

Full-replaces an existing notification preference. `emailEnabled`, `smsEnabled`, `pushEnabled`, and `inAppEnabled` are only overwritten when supplied; a missing field keeps its current stored value. `userId` is re-resolved and overwritten unconditionally. No uniqueness re-check is performed on `(userId, notificationType)` at update time. This entity has no `createdAt` column — only `updatedAt` is ever stamped.

---

## Endpoint

```json
PUT /api/v1/notification-preferences/{notificationPreferenceId}
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
	"emailEnabled": false,
	"smsEnabled": false,
	"pushEnabled": true,
	"inAppEnabled": true,
	"quietHoursStart": "22:00:00",
	"quietHoursEnd": "07:00:00"
}
```

## Successful Response

```json
200 OK
```

```json
{
	"notificationPreferenceId": 0,
	"userId": 0,
	"notificationType": "",
	"emailEnabled": false,
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
- 404 NOTIFICATION_PREFERENCE_NOT_FOUND
- 404 USER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "NOTIFICATION_PREFERENCE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/notification-preferences/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "userId": 1,
	"notificationType": "BOOKING_CONFIRMED",
	"emailEnabled": false,
	"smsEnabled": false,
	"pushEnabled": true,
	"inAppEnabled": true,
	"quietHoursStart": "22:00:00",
	"quietHoursEnd": "07:00:00"
}'
```

---

## Event Produced

- NotificationPreferenceUpdated
