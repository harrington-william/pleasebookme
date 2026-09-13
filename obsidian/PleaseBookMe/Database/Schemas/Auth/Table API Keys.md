# `auth.api_keys`

> **Variant:** `ENTITY`

## Purpose

Issues long-lived programmatic credentials scoped to a tenant, for machine-to-machine access to the platform API outside the interactive login flow.

## Fields

| Field | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Internal surrogate primary key. |
| `uid` | `UUID` | Stable, externally-safe identifier for this key. |
| `tenant_id` | `BIGINT` | The tenant this key is scoped to. |
| `owner_user_id` | `BIGINT` | The platform user accountable for this key. |
| `name` | `VARCHAR(255)` | Caller-assigned label for the key. |
| `description` | `TEXT` | Optional description. |
| `public_key` | `TEXT` | Public, non-secret identifier for the key; unique. |
| `secret_hash` | `TEXT` | Hashed form of the key's bearer secret. |
| `status` | `auth.api_key_status` | `ACTIVE` / `REVOKED` / `EXPIRED`. |
| `last_used_at` | `TIMESTAMPTZ` | Optional timestamp of last use. |
| `expires_at` | `TIMESTAMPTZ` | Optional expiry timestamp. |
| `revoked_at` | `TIMESTAMPTZ` | Optional revocation timestamp. |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Row creation/last-modified timestamps. |

## Row Semantics

Each row represents one programmatic API credential issued for a tenant, accountable to the platform user who owns it.

## Ownership

- **Domain:** Auth
- **Module:** Programmatic Access
- **Scope:** Tenant → owning User. Unlike most of `auth`'s otherwise platform-wide tables, this one is directly tenant-scoped.

## Lifecycle

Standard CRUD create/update/delete via `ApiKeyServiceImpl`/`ApiKeyController`. `status` models `ACTIVE`/`REVOKED`/`EXPIRED`, but no application code observed in this session actually drives an automatic transition between them (no scheduled expiry sweep, no dedicated revoke endpoint) — a status change currently relies on a full-replace `PUT` supplying the new value directly. Per the project's own documentation (`AGENTS.md`), this table was created ahead of the feature that would functionally consume it: no authentication path was found in this session that resolves a request against `auth.api_keys`.

## Invariants

- `public_key` is globally unique.
- `secret_hash` is expected to hold a hash, never a plaintext secret — `ApiKeyResponse` keeps `publicKey` (a safe public identifier) but drops `secretHash` from the API response.
- `tenant_id` is resolved in `ApiKeyServiceImpl` as a bare reference entity (`TenantEntity.builder().tenantId(...).build()`), not a validated repository lookup — an invalid `tenantId` is not caught as a clean 404, it surfaces as a raw foreign-key-violation error from the database.

## Relationships

- **Tenant:** The subscription/tenant this key is scoped to — the isolation boundary a request authenticated by this key should be constrained to, once such a path exists.
- **User (owner):** The platform user accountable for this key's existence and usage.

## Usage Rules

- Writes go through `ApiKeyServiceImpl`.
- `secretHash` must never be returned by the API — already enforced by `ApiKeyResponse`'s field selection.

## Security Considerations

This table is a credential store: `secret_hash` should never appear in logs, responses, or documentation examples. `status`/`expires_at`/`revoked_at` are the fields intended to gate whether a key is currently usable, but **Undetermined**: no request-time enforcement path checking these fields against an incoming API key was located in this session — only the CRUD layer around the table itself was inspected.

## Flagged for Follow-up

- Confirm whether any authentication path actually resolves an incoming request against `auth.api_keys` yet. Only the CRUD layer was inspected; no filter/interceptor checking `public_key`/`secret_hash`/`status`/`expires_at` against a live request was located. Per `AGENTS.md`, this table may still be ahead of the feature that consumes it.
- If no such path exists yet, `tenant_id`'s bare-reference-entity resolution (no validated lookup) in `ApiKeyServiceImpl` should be upgraded to a real `TenantRepository.findById().orElseThrow()` call when this domain is next touched, consistent with the platform's standing "upgrade once the repository exists" convention.
