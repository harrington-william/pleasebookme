## Description

Retrieves every widget origin.

---

## Endpoint

```json
GET /api/v1/widget-origins
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
		"widgetOriginId": 0,
		"widgetId": 0,
		"origin": "",
		"verified": false,
		"createdById": 0,
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
<https://api.pleasebookme.com/api/v1/widget-origins> \
-H "Authorization: Bearer xxx"
```
