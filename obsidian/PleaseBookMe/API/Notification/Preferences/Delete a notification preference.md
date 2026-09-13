## Description

Deletes a notification preference.

---

## Endpoint

```json
DELETE /api/v1/notification-preferences/{notificationPreferenceId}
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
204 No Content
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOTIFICATION_PREFERENCE_NOT_FOUND
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
-X DELETE \
<https://api.pleasebookme.com/api/v1/notification-preferences/1> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- NotificationPreferenceDeleted
