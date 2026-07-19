CREATE TABLE tenant.ecosystems (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    icon VARCHAR(100) NOT NULL,
    status tenant.ecosystem_status NOT NULL DEFAULT 'REVIEWING',
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_tenant_ecosystems_code UNIQUE (code)
);
