CREATE TYPE integration.oauth_provider AS ENUM (
    'GOOGLE'
);

CREATE TYPE integration.oauth_connection_status AS ENUM (
    'ACTIVE',
    'REVOKED',
    'EXPIRED',
    'ERROR'
);

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
