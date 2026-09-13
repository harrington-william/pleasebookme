## Description

Records where a customer was acquired from (e.g. a marketing channel or referral).

---

## Endpoint

```json
POST /api/v1/customer-sources
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
	"source": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"customerSourceId": 0,
	"customerId": 0,
	"source": "",
	"createdAt": ""
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
<https://api.pleasebookme.com/api/v1/customer-sources> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "customerId": 1,
	"source": "FACEBOOK_ADS"
}'
```

---

## Event Produced

- CustomerSourceCreated
