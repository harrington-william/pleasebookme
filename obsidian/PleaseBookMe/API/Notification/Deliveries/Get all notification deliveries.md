## Description

Retrieves every notification delivery attempt.

---

## Endpoint

```json
GET /api/v1/notification-deliveries
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
		"notificationDeliveryId": 0,
		"notificationId": 0,
		"provider": "",
		"providerMessageId": "",
		"status": "SENT",
		"attempt": 1,
		"errorMessage": null,
		"sentAt": "",
		"createdAt": ""
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
<https://api.pleasebookme.com/api/v1/notification-deliveries> \
-H "Authorization: Bearer xxx"
```
