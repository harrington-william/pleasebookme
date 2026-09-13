# Generate widget credentials

## Description

Generates a new public/secret key pair without persisting anything. This is the only widget endpoint that returns a plaintext secret; the client must retain it and immediately pass the pair to create or credential rotation because the secret cannot be retrieved later.

---

## Endpoint

```json
POST /api/v1/widgets/credentials
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
	"publicKey": "pbm_pk_FjqMnajjAJXQM0AGIIHQAQ",
	"secretKey": "pbm_sk_<43 URL-safe Base64 characters>"
}
```

---

## Possible Errors

- 401 unauthenticated

---

## Example Request

```bash
curl \
-X POST \
<https://api.pleasebookme.com/api/v1/widgets/credentials> \
-H "Authorization: Bearer xxx"
```
