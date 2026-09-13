## Description

Full-replaces an existing destination sheets link. All three foreign keys (`userId`, `serviceId`, `oauthConnectionId`) are re-resolved on every call — the caller is expected to send the complete resource.

---

## Endpoint

```json
PUT /api/v1/destination-sheets/{destinationSheetsId}
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
200 OK
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
- 404 DESTINATION_SHEETS_NOT_FOUND
- 404 USER_NOT_FOUND
- 404 SERVICE_NOT_FOUND
- 404 OAUTH_CONNECTION_NOT_FOUND
- 429 RATE_LIMIT_EXCEEDED

---

## Error Message

```json
{
	"code": "DESTINATION_SHEETS_NOT_FOUND"
}
```

---

## Example Request

```bash
curl \
-X PUT \
<https://api.pleasebookme.com/api/v1/destination-sheets/1> \
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

- DestinationSheetsUpdated
