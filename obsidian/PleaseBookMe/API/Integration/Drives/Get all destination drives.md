## Description

Retrieves every destination drive link.

---

## Endpoint

```json
GET /api/v1/destination-drives
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
		"destinationDriveId": 0,
		"integrationType": "",
		"externalId": "",
		"userId": 0,
		"serviceId": 0,
		"oauthConnectionId": 0,
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
<https://api.pleasebookme.com/api/v1/destination-drives> \
-H "Authorization: Bearer xxx"
```
