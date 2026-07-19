CREATE TABLE customer.customers (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    tenant_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    avatar_url TEXT,
    locale public.locale,
    timezone VARCHAR(100),
    birthday DATE NOT NULL,
    gender VARCHAR(20),
    status VARCHAR(50) NOT NULL,
    marketing_consent BOOLEAN NOT NULL DEFAULT false,
    notes TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_customers_uid UNIQUE (uid),
    CONSTRAINT uq_customers_tenant_email UNIQUE (tenant_id, email),
    CONSTRAINT uq_customers_tenant_phone UNIQUE (tenant_id, phone),

    CONSTRAINT fk_customer_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant.tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_customer_organization
        FOREIGN KEY (organization_id)
        REFERENCES organization.organizations(id)
        ON DELETE CASCADE
);
