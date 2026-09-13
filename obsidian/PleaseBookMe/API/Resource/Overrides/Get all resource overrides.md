## Description

Retrieves every resource override.

---

## Endpoint

```json
GET /api/v1/resource-overrides
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
		"resourceOverrideId": 0,
		"resourceId": 0,
		"startTime": "",
		"endTime": "",
		"reason": "",
		"createdAt": "",
		"updatedAt": ""
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
<https://api.pleasebookme.com/api/v1/resource-overrides> \
-H "Authorization: Bearer xxx"
```
