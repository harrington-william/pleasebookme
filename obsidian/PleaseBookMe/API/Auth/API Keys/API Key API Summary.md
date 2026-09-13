# Overview

The API Key API allows application to create, retrieve, update, and delete API keys. An API key is scoped to a tenant and owned by a user, and is the credential future developer/service-account integrations will authenticate with.

## Base URL

```bash
/api/v1/api-keys
```

## Protocol

```bash
HTTPS
```

## Response Format

```bash
application/json
```

## Character Encoding

```bash
UTF-8
```

## Authentication

```bash
Every endpoint in this resource requires a **Bearer Token**
```

---

# Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/v1/api-keys | [[Create an API key]] |
| GET | /api/v1/api-keys/{apiKeyId} | [[Get an API key]] |
| GET | /api/v1/api-keys | [[Get all API keys]] |
| PUT | /api/v1/api-keys/{apiKeyId} | [[Update an API key]] |
| DELETE | /api/v1/api-keys/{apiKeyId} | [[Delete an API key]] |
