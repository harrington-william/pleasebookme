CREATE TABLE tenant.domains (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    domain VARCHAR(255) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT false,
    is_primary BOOLEAN NOT NULL DEFAULT true,
    verification_token TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_tenant_domains_domain UNIQUE (domain),

    CONSTRAINT fk_tenant_domain_tenant FOREIGN KEY (tenant_id) REFERENCES tenant.tenants(id) ON DELETE CASCADE
);
