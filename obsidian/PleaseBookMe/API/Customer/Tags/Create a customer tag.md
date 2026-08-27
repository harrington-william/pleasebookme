## Description

Attaches a tag to a customer. `tag` must be unique per customer — tagging the same customer with the same tag twice is rejected.

---

## Endpoint

```json
POST /api/v1/customer-tags
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
	"tag": ""
}
```

## Successful Response

```json
201 Created
```

```json
{
	"customerTagId": 0,
	"customerId": 0,
	"tag": "",
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 CUSTOMER_NOT_FOUND
- 409 CUSTOMER_TAG_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "CUSTOMER_TAG_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/customer-tags> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "customerId": 1,
	"tag": "VIP"
}'
```

---

## Event Produced

- CustomerTagCreated
