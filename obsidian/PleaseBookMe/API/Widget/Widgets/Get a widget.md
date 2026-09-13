# Get a widget

## Description

Retrieves a widget and its normalized registered origin by numeric ID. Direct reads include revoked widgets; revocation only removes them from the paginated list and active total.

---

## Endpoint

```json
GET /api/v1/widgets/{widgetId}
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
	"widgetId": 3,
	"widgetUid": "1076a995-6814-4b25-a255-c67949809f25",
	"tenantId": 4,
	"name": "Dashboard Widget",
	"status": "REVOKED",
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

- 401 unauthenticated
- 404 widget not found

---

## Example Request

```bash
curl \
<https://api.pleasebookme.com/api/v1/widgets/3> \
-H "Authorization: Bearer xxx"
```
