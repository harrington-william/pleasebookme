## Description

Full-replace semantics — the caller is expected to send the complete resource. `verified` is only overwritten when supplied; a missing field keeps its current stored value. `createdById` is re-resolved from the request on every call — omitting it clears the association. The composite `(widgetId, origin)` uniqueness is not re-checked on update.

---

## Endpoint

```json
PUT /api/v1/widget-origins/{widgetOriginId}
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
200 OK
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
- 404 WIDGET_ORIGIN_NOT_FOUND
- 404 WIDGET_NOT_FOUND
- 404 USER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "WIDGET_ORIGIN_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/widget-origins/1> \
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

- WidgetOriginUpdated
