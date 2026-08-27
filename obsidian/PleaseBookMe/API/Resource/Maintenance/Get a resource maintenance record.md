## Description

Retrieves a single resource maintenance record by its numeric ID.

---

## Endpoint

```json
GET /api/v1/resource-maintenance/{resourceMaintenanceId}
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
	"resourceMaintenanceId": 0,
	"resourceId": 0,
	"startTime": "",
	"endTime": "",
	"reason": "",
	"status": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_MAINTENANCE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_MAINTENANCE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/resource-maintenance/1> \
-H "Authorization: Bearer xxx"
```
