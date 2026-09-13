## Description

Retrieves every customer activity entry.

---

## Endpoint

```json
GET /api/v1/customer-activities
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
		"customerActivityId": 0,
		"customerId": 0,
		"activityType": "",
		"referenceType": "",
		"referenceUid": "",
		"description": "",
		"occurredAt": ""
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
<https://api.pleasebookme.com/api/v1/customer-activities> \
-H "Authorization: Bearer xxx"
```
