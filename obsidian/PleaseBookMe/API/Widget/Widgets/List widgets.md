# List widgets

## Description

Lists the caller's tenant widgets for one organization. `organizationId` is required; when it does not match the caller's current organization, the endpoint returns an empty page.

Revoked widgets are always excluded, including when `status=REVOKED`. Results are newest-first by default, unknown sort properties are ignored, page size defaults to 20, and sizes above 100 are capped at 100. Each item includes its normalized registered origin without issuing a query per widget.

---

## Endpoint

```json
GET /api/v1/widgets
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

## Query Parameters

| Parameter | Required | Description |
|---|---|---|
| `organizationId` | Yes | Organization whose tenant widgets should be listed. |
| `type` | No | `INLINE`, `POPUP`, `FULL_PAGE`, or `EMBEDDED`. |
| `status` | No | Status filter. `REVOKED` deliberately returns an empty page. |
| `page` | No | Zero-based page number; defaults to `0`. |
| `size` | No | Page size; defaults to `20` and is capped at `100`. |
| `sort` | No | Sort by `name`, `status`, `type`, `createdAt`, or `updatedAt`; defaults to `createdAt,desc`. |

## Successful Response

```json
200 OK
```

```json
{
	"content": [
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
	],
	"page": 0,
	"size": 20,
	"totalElements": 1,
	"totalPages": 1
}
```

---

## Possible Errors

- 400 missing or malformed query parameter
- 401 unauthenticated
- 403 no accepted organization membership
- 404 organization has no provisioned tenant
- 409 ambiguous organization context

---

## Example Request

```bash
curl \
<https://api.pleasebookme.com/api/v1/widgets?organizationId=4&type=INLINE&page=0&size=20> \
-H "Authorization: Bearer xxx"
```
