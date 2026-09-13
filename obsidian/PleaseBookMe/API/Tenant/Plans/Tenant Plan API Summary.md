# Overview

The Tenant Plan API allows application to create, retrieve, update, and delete subscription plans — the resource-quota tiers (e.g. FREE, PRO) tenants are assigned to.

## Base URL

```bash
/api/v1/tenant-plans
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
| POST | /api/v1/tenant-plans | [[Create a tenant plan]] |
| GET | /api/v1/tenant-plans/{tenantPlanId} | [[Get a tenant plan]] |
| GET | /api/v1/tenant-plans | [[Get all tenant plans]] |
| PUT | /api/v1/tenant-plans/{tenantPlanId} | [[Update a tenant plan]] |
| DELETE | /api/v1/tenant-plans/{tenantPlanId} | [[Delete a tenant plan]] |
