## Description

Links a service to an external spreadsheet (e.g. a connected Google Sheet) that bookings for that service should sync to. Requires an existing, already-established `oauthConnection` — this endpoint does not perform any OAuth handshake itself, it only records which external sheet a service writes to.

---

## Endpoint

```json
POST /api/v1/destination-sheets
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
	"integrationType": "",
	"externalId": "",
	"userId": 0,
	"serviceId": 0,
	"oauthConnectionId": 0
}
```

## Successful Response

```json
201 Created
```

```json
{
	"destinationSheetsId": 0,
	"integrationType": "",
	"externalId": "",
	"userId": 0,
	"serviceId": 0,
	"oauthConnectionId": 0,
	"createdAt": "",
	"updatedAt": ""
}
```

---

## Possible Errors

- 400 INVALID_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 USER_NOT_FOUND
- 404 SERVICE_NOT_FOUND
- 404 OAUTH_CONNECTION_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "OAUTH_CONNECTION_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/destination-sheets> \
-H "Authorization: Bearer xxx" \
-H "Idempotency-Key: 123456" \
-H "Content-Type: application/json" \
-d '{
    "integrationType": "GOOGLE_SHEETS",
	"externalId": "1BxA2c3D4eFGhIjKlMnOpQrStUvWxYz",
	"userId": 1,
	"serviceId": 1,
	"oauthConnectionId": 1
}'
```

---

## Event Produced

- DestinationSheetsCreated
