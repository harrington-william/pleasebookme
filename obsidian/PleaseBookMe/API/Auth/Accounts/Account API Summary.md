# Overview

The Account API allows application to create, retrieve, update, and delete OAuth provider linkages (e.g. Google) for a user. An account row does not hold live OAuth credentials — no access/refresh/ID token is stored on it or ever returned.

## Base URL

```bash
/api/v1/accounts
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
| POST | /api/v1/accounts | [[Create an account]] |
| GET | /api/v1/accounts/{accountId} | [[Get an account]] |
| GET | /api/v1/accounts | [[Get all accounts]] |
| PUT | /api/v1/accounts/{accountId} | [[Update an account]] |
| DELETE | /api/v1/accounts/{accountId} | [[Delete an account]] |
