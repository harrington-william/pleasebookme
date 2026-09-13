## Description

Creates a customer under a tenant/organization. `email` and `phone` must each be unique within the owning tenant — a collision on either is rejected before the tenant/organization are even resolved.

---

## Endpoint

```json
POST /api/v1/customers
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
201 Created
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
- 404 TENANT_NOT_FOUND
- 404 ORGANIZATION_NOT_FOUND
- 409 CUSTOMER_EMAIL_ALREADY_EXISTS
- 409 CUSTOMER_PHONE_ALREADY_EXISTS
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "CUSTOMER_EMAIL_ALREADY_EXISTS"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/customers> \
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

- CustomerCreated
