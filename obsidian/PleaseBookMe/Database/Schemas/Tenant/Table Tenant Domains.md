# `tenant.tenant_domains`

> **Variant:** `ENTITY`

## Purpose

Registers a custom domain a tenant wants to serve their booking presence from, with a DNS-verification workflow.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `tenant_id` | `BIGINT` | The tenant this domain belongs to. |
| `domain` | `VARCHAR(255)` | The custom domain, globally unique. |
| `verified` | `BOOLEAN` | Whether ownership has been verified. |
| `is_primary` | `BOOLEAN` | Whether this is the tenant's primary domain. |
| `verification_token` | `TEXT` | The value the tenant must publish (e.g. as a DNS TXT record) to prove ownership. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one custom domain a tenant has registered, along with its verification state.

## Ownership

- **Domain:** Tenant
- **Module:** SaaS Account Configuration
- **Scope:** Tenant.

## Lifecycle

Created when a tenant registers a custom domain. `verified` presumably transitions from `false` to `true` once DNS ownership is confirmed, though no dedicated verification-check endpoint was located in this session — a status change currently relies on full-replace `PUT`.

## Invariants

- `domain` is globally unique across all tenants.

## Relationships

- **Tenant:** The tenant this domain belongs to.

## Usage Rules

- Writes go through `TenantDomainServiceImpl` (`tenant/domain/`).

## Important Fields

- `verification_token` — **Deliberately kept in the API response**, unlike every other token/secret/credential column in the schema. This value is a DNS-verification token the client is expected to read back and publish externally (e.g. as a DNS TXT record) to prove domain ownership — it is not a bearer credential used to authenticate as the tenant, so the platform's standing "never echo a stored credential" rule does not apply to it.
