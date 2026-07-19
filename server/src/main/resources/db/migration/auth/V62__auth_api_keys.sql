CREATE TABLE auth.api_keys (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    tenant_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    public_key TEXT NOT NULL,
    secret_hash TEXT NOT NULL,
    status auth.api_key_status NOT NULL DEFAULT 'ACTIVE',
    last_used_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_api_keys_uid UNIQUE (uid),
    CONSTRAINT uq_api_keys_public_key UNIQUE (public_key),

    CONSTRAINT fk_api_key_tenant FOREIGN KEY (tenant_id) REFERENCES tenant.tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_api_key_owner FOREIGN KEY (owner_user_id) REFERENCES auth.users(id) ON DELETE CASCADE
);
