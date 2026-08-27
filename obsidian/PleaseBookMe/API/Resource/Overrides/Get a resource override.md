## Description

Retrieves a single resource override by its numeric ID.

---

## Endpoint

```json
GET /api/v1/resource-overrides/{resourceOverrideId}
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
	"resourceOverrideId": 0,
	"resourceId": 0,
	"startTime": "",
	"endTime": "",
	"reason": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 RESOURCE_OVERRIDE_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "RESOURCE_OVERRIDE_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/resource-overrides/1> \
-H "Authorization: Bearer xxx"
```
