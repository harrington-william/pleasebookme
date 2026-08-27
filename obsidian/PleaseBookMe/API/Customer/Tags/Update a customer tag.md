## Description

Full-replace semantics — the caller is expected to send the complete resource. `customerId` is re-resolved on every call. No uniqueness re-check is performed on `tag` at update time.

---

## Endpoint

```json
PUT /api/v1/customer-tags/{customerTagId}
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
200 OK
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
- 404 CUSTOMER_TAG_NOT_FOUND
- 404 CUSTOMER_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "CUSTOMER_TAG_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/customer-tags/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "customerId": 1,
	"tag": "REGULAR"
}'
```

---

## Event Produced

- CustomerTagUpdated
