## Description

Retrieves a single widget origin by its numeric ID.

---

## Endpoint

```json
GET /api/v1/widget-origins/{widgetOriginId}
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
-X GET \
<https://api.pleasebookme.com/api/v1/widget-origins/1> \
-H "Authorization: Bearer xxx"
```
