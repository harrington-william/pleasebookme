## Description

Retrieves every resource maintenance record.

---

## Endpoint

```json
GET /api/v1/resource-maintenance
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
		"resourceMaintenanceId": 0,
		"resourceId": 0,
		"startTime": "",
		"endTime": "",
		"reason": "",
		"status": "",
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
<https://api.pleasebookme.com/api/v1/resource-maintenance> \
-H "Authorization: Bearer xxx"
```
