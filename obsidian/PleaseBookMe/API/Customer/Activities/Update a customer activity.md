## Description

Full-replace semantics — the caller is expected to send the complete resource. `customerId` is re-resolved on every call.

---

## Endpoint

```json
PUT /api/v1/customer-activities/{customerActivityId}
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

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 CUSTOMER_ACTIVITY_NOT_FOUND
- 404 CUSTOMER_NOT_FOUND
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
-X PUT \
<https://api.pleasebookme.com/api/v1/customer-activities/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "customerId": 1,
	"activityType": "BOOKING_CANCELLED",
	"referenceType": "BOOKING",
	"referenceUid": "9f1c1e0a-1234-4a5b-8c9d-000000000001",
	"description": "Customer cancelled the appointment"
}'
```

---

## Event Produced

- CustomerActivityUpdated
