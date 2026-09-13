# Overview

The Tenant Domain API allows application to create, retrieve, update, and delete custom domains registered against a tenant (e.g. a client's own website domain).

## Base URL

```bash
/api/v1/tenant-domains
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
| POST | /api/v1/tenant-domains | [[Create a tenant domain]] |
| GET | /api/v1/tenant-domains/{tenantDomainId} | [[Get a tenant domain]] |
| GET | /api/v1/tenant-domains | [[Get all tenant domains]] |
| PUT | /api/v1/tenant-domains/{tenantDomainId} | [[Update a tenant domain]] |
| DELETE | /api/v1/tenant-domains/{tenantDomainId} | [[Delete a tenant domain]] |
