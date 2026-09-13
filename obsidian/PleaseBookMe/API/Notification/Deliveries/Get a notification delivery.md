## Description

Retrieves a single notification delivery attempt by its numeric ID.

---

## Endpoint

```json
GET /api/v1/notification-deliveries/{notificationDeliveryId}
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
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOTIFICATION_DELIVERY_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "NOTIFICATION_DELIVERY_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/notification-deliveries/1> \
-H "Authorization: Bearer xxx"
```
