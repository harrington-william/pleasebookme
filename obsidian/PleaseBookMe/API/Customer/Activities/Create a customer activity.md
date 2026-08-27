## Description

Records an activity-log entry against a customer.

---

## Endpoint

```json
POST /api/v1/customer-activities
```

---

## Authentication

Required **Bearer Token**

---

## Headers

```json
{
	"Authorization": "JWT Access Token",
	"Content-Type": "application/json"
}
```

## Body

```json
{
	"customerId": 0,
	"activityType": "",
	"referenceType": "",
	"referenceUid": "",
	"description": ""
}
```

## Successful Response

```json
201 Created
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

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 CUSTOMER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "CUSTOMER_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/customer-activities> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "customerId": 1,
	"activityType": "BOOKING_CREATED",
	"referenceType": "BOOKING",
	"referenceUid": "9f1c1e0a-1234-4a5b-8c9d-000000000001",
	"description": "Customer booked a haircut appointment"
}'
```

---

## Event Produced

- CustomerActivityCreated
