# Create a widget

## Description

Creates an active widget for the caller's provisioned tenant. The client first obtains a credential pair from [[Generate widget credentials]] and sends it under `credentials`; the secret is BCrypt-hashed at rest and is never returned by this endpoint.

`type` defaults to `EMBEDDED`. A supplied origin is normalized to `scheme://host[:non-default-port]`; paths, queries, fragments, and default ports are removed. A missing or blank origin creates no origin row and disables origin validation. `expiresAt` and `lastUsedAt` remain `null` because they are server-owned and this flow does not set them.

---

## Endpoint

```json
POST /api/v1/widgets
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
	"name": "Dashboard Widget",
	"type": "INLINE",
	"origin": "Barbershop.com/book?x=1",
	"credentials": {
		"publicKey": "pbm_pk_FjqMnajjAJXQM0AGIIHQAQ",
		"secretKey": "pbm_sk_<43 URL-safe Base64 characters>"
	}
}
```

`name` and `credentials` are required. `publicKey` must match `^pbm_pk_[A-Za-z0-9_-]{22}$`; `secretKey` must match `^pbm_sk_[A-Za-z0-9_-]{43}$`.

## Successful Response

```json
201 Created
```

```json
{
	"widgetId": 3,
	"widgetUid": "1076a995-6814-4b25-a255-c67949809f25",
	"tenantId": 4,
	"name": "Dashboard Widget",
	"status": "ACTIVE",
	"type": "INLINE",
	"originValidation": true,
	"publicKey": "pbm_pk_FjqMnajjAJXQM0AGIIHQAQ",
	"origin": "https://barbershop.com",
	"issuedAt": "2026-09-11T18:13:15.157954Z",
	"expiresAt": null,
	"lastUsedAt": null,
	"createdAt": "2026-09-11T18:13:15.158232Z",
	"updatedAt": "2026-09-11T18:13:15.158237Z"
}
```

---

## Possible Errors

- 400 invalid request or origin
- 401 unauthenticated
- 403 no accepted organization membership
- 404 organization has no provisioned tenant
- 409 public key already exists or organization context is ambiguous

---

## Event Produced

- WidgetCreated
