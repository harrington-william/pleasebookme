## Description

Full-replace semantics — the caller is expected to send the complete resource. `tenantId`/`organizationId` are re-resolved on every call. No uniqueness re-check is performed on `email`/`phone` at update time.

---

## Endpoint

```json
PUT /api/v1/customers/{customerId}
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
	"tenantId": 0,
	"organizationId": 0,
	"email": "",
	"phone": "",
	"name": "",
	"avatarUrl": "",
	"locale": "",
	"timezone": "",
	"birthday": "",
	"gender": "",
	"status": "",
	"marketingConsent": false,
	"notes": ""
}
```

## Successful Response

```json
200 OK
```

```json
{
	"customerId": 0,
	"customerUid": "",
	"tenantId": 0,
	"organizationId": 0,
	"email": "",
	"phone": "",
	"name": "",
	"avatarUrl": "",
	"locale": "",
	"timezone": "",
	"birthday": "",
	"gender": "",
	"status": "",
	"marketingConsent": false,
	"notes": "",
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
- 404 TENANT_NOT_FOUND
- 404 ORGANIZATION_NOT_FOUND
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
-X PUT \
<https://api.pleasebookme.com/api/v1/customers/1> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "tenantId": 1,
	"organizationId": 1,
	"email": "customer@gmail.com",
	"phone": "0912345678",
	"name": "Nguyen Van A",
	"avatarUrl": "",
	"locale": "vi",
	"timezone": "Asia/Ho_Chi_Minh",
	"birthday": "1995-05-20",
	"gender": "MALE",
	"status": "ACTIVE",
	"marketingConsent": true,
	"notes": ""
}'
```

---

## Event Produced

- CustomerUpdated
