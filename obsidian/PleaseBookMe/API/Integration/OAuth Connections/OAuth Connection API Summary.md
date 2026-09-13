# Overview

The OAuth Connection API allows application to retrieve and delete a user's stored OAuth grant (e.g. a connected Google account). It is deliberately **not** a full CRUD surface: an `oauth_connections` row holds live, encrypted third-party credentials, so it may only ever be written by the dedicated Google consent flow (`/api/v1/integrations/google/connect` → callback), never from a client-supplied request body. `POST`, `PUT`, and a list-all endpoint were removed on purpose — allowing `POST`/`PUT` here would let a caller inject forged tokens, and a list-all would expose every user's connections to any authenticated caller. Only an owner-scoped `GET` and `DELETE` remain.

## Base URL

```bash
/api/v1/oauth-connections
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
| GET | /api/v1/oauth-connections/{oauthConnectionId} | [[Get an OAuth connection]] |
| DELETE | /api/v1/oauth-connections/{oauthConnectionId} | [[Delete an OAuth connection]] |
