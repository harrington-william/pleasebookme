-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 13 - Integration - OAuth Connections
-- =====================================================

-- Delegated-authorization grant to a third-party provider's API
-- (Calendar/Sheets/Drive today, Outlook or others later), distinct from
-- auth.accounts (V17), which only links a login identity. Provider-
-- agnostic by design (`provider` enum, not a Google-specific table
-- name) - the connection shape (scoped token pair, expiry, revocation,
-- refresh bookkeeping) is identical across any OAuth2 provider this
-- platform will integrate, the same generalization already made for
-- integration.integration_type.
CREATE TYPE integration.oauth_provider AS ENUM (
    'GOOGLE'
);

-- 'ERROR' covers a refresh attempt that failed for a reason other than
-- revocation (expired refresh token, provider outage) - kept distinct
-- from 'EXPIRED' so a worker can tell "Google rejected us" apart from
-- "we haven't refreshed in time yet".
CREATE TYPE integration.oauth_connection_status AS ENUM (
    'ACTIVE',
    'REVOKED',
    'EXPIRED',
    'ERROR'
);

-- access_token/refresh_token are application-layer encrypted ciphertext
-- (AES-GCM), never plaintext - this is a bearer credential to a
-- customer's real Google account, not our own login credential, so the
-- V11-style exception does not extend here. token_key_version supports
-- encryption-key rotation without re-encrypting every row atomically
-- in one migration.
-- refresh_token is NOT NULL: a connection without one cannot be
-- refreshed past its first access-token expiry (~1 hour) and is
-- useless for the async sync flow this table exists to support, so the
-- application must request access_type=offline&prompt=consent and
-- refuse to persist a connection that didn't return one.
-- provider_email is independent of auth.accounts.provider_email - the
-- Google account granting API access is not guaranteed to be the same
-- one the user signed in with.
CREATE TABLE integration.oauth_connections (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    provider integration.oauth_provider NOT NULL,
    provider_account_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    scopes TEXT[] NOT NULL,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    token_key_version SMALLINT NOT NULL DEFAULT 1,
    token_expires_at TIMESTAMPTZ NOT NULL,
    status integration.oauth_connection_status NOT NULL DEFAULT 'ACTIVE',
    connected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_refreshed_at TIMESTAMPTZ,
    last_used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_oauth_connections_uid UNIQUE (uid),
    CONSTRAINT uq_oauth_connections_user_provider_account UNIQUE (user_id, provider, provider_account_id),

    CONSTRAINT fk_oauth_connection_user
        FOREIGN KEY (user_id)
        REFERENCES auth.users(id)
        ON DELETE CASCADE
);
