## Description

Deletes a widget origin.

---

## Endpoint

```json
DELETE /api/v1/widget-origins/{widgetOriginId}
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
- 404 WIDGET_ORIGIN_NOT_FOUND
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
-X DELETE \
<https://api.pleasebookme.com/api/v1/widget-origins/1> \
-H "Authorization: Bearer xxx"
```

---

## Event Produced

- WidgetOriginDeleted
