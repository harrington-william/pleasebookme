# Get widget stats

## Description

Returns widget counts for the caller's tenant in one organization. `total` excludes revoked widgets, while `revoked` reports them separately. A foreign `organizationId` returns all-zero counts.

---

## Endpoint

```json
GET /api/v1/widgets/stats
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
| `organizationId` | Yes | Organization whose tenant widget counts should be returned. |

## Successful Response

```json
200 OK
```

```json
{
	"total": 1,
	"active": 1,
	"disabled": 0,
	"revoked": 2
}
```

---

## Possible Errors

- 400 missing or malformed `organizationId`
- 401 unauthenticated
- 403 no accepted organization membership
- 404 organization has no provisioned tenant
- 409 ambiguous organization context

---

## Example Request

```bash
curl \
<https://api.pleasebookme.com/api/v1/widgets/stats?organizationId=4> \
-H "Authorization: Bearer xxx"
```
