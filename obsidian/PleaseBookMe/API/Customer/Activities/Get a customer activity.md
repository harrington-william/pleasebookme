## Description

Retrieves a single customer activity entry by its numeric ID.

---

## Endpoint

```json
GET /api/v1/customer-activities/{customerActivityId}
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
	"customerActivityId": 0,
	"customerId": 0,
	"activityType": "",
	"referenceType": "",
	"referenceUid": "",
	"description": "",
	"occurredAt": ""
}
```

---

## Possible Errors

- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 CUSTOMER_ACTIVITY_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "CUSTOMER_ACTIVITY_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X GET \
<https://api.pleasebookme.com/api/v1/customer-activities/1> \
-H "Authorization: Bearer xxx"
```
