## Description

Registers an allowed origin domain for a widget. A widget with origin validation enabled will reject requests from any origin not registered here. `(widgetId, origin)` must be unique. `verified` defaults to `false` when omitted. `createdById` is optional — omit it when the origin isn't attributed to a specific user.

---

## Endpoint

```json
POST /api/v1/widget-origins
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
	"widgetId": 0,
	"origin": "",
	"verified": false,
	"createdById": 0
}
```

## Successful Response

```json
201 Created
```

```json
{
	"widgetOriginId": 0,
	"widgetId": 0,
	"origin": "",
	"verified": false,
	"createdById": 0,
	"createdAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 WIDGET_NOT_FOUND
- 404 USER_NOT_FOUND
- 409 WIDGET_ORIGIN_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "WIDGET_ORIGIN_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/widget-origins> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "widgetId": 1,
	"origin": "https://barbershop.com",
	"verified": true,
	"createdById": 1
}'
```

---

## Event Produced

- WidgetOriginCreated
