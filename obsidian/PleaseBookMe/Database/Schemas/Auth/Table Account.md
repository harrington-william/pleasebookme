# `auth.accounts`

> **Variant:** `ENTITY`

## Purpose

Records the linkage between a platform user and an external identity-provider account (currently Google), used to resolve a returning OAuth sign-in to the correct platform user.

## Fields

| Field                 | Type           | Description                                                                       |
| --------------------- | -------------- | --------------------------------------------------------------------------------- |
| `id`                  | `BIGSERIAL`    | Internal surrogate primary key.                                                   |
| `user_id`             | `BIGINT`       | The platform user this external account is linked to.                             |
| `type`                | `VARCHAR(50)`  | Account link type; defaults to `oauth`.                                           |
| `provider`            | `VARCHAR(100)` | The external identity provider (e.g. GOOGLE).                                     |
| `provider_account_id` | `VARCHAR(255)` | The provider's stable identifier for this external account (e.g. Google's `sub`). |
| `provider_email`      | `VARCHAR(255)` | Optional email address reported by the provider at link time.                     |

## Row Semantics

Each row represents one linkage between a platform user and one external-provider account identity — not a session, and not a delegated-authorization grant, only "this external identity maps to this platform user."

## Ownership

- **Domain:** Auth
- **Module:** Identity (OAuth sign-in linkage)
- **Scope:** User

## Lifecycle

Created by `GoogleAccountResolver` during Google Sign-In account resolution — specifically when linking an existing user found by verified email, and implicitly whenever a new user is provisioned through Google Sign-In or the Google one-shot registration flow. Also directly creatable via generic CRUD (`POST /api/v1/accounts`). No revoke/disable workflow was found in code; deletion is physical via `DELETE /api/v1/accounts/{accountId}`.

## Invariants

- `(provider, provider_account_id)` is globally unique — this is what lets `GoogleAccountResolver`'s first resolution step ("is this external identity already linked?") be a single lookup.
- This table stores no OAuth token material. `access_token`, `refresh_token`, `expires_at`, `token_type`, `scope`, and `id_token` were all removed by migration `V113__auth_accounts_drop_oauth_tokens.sql` — live Google credentials live exclusively in `integration.oauth_connections`. This table is identity-linkage only.
- This table has no `created_at`/`updated_at` columns.

## Relationships

- **User:** The platform user this external account is linked to.
- **Delegated authorization (conceptual, not a foreign key):** `integration.oauth_connections` holds any live Google credential for the same external identity. The two tables concern the same provider account but are deliberately not joined at the schema level — Sign-In identity linkage and delegated-authorization credentials are treated as architecturally distinct concerns.

## Usage Rules

- Writes go through `AccountServiceImpl`, or implicitly through `GoogleAccountResolver` during sign-in/registration.
- Do not repurpose this table to store any live token/credential material — the removal of the token columns in `V113` was a deliberate architectural separation, not an oversight to "fix" by adding them back.

## Security Considerations

`provider_account_id`/`provider_email` identify a real external account and should be treated as personal data, but this table holds no bearer credentials as of the current schema — see Invariants.
