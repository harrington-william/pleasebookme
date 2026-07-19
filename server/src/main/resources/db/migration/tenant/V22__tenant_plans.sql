CREATE TABLE tenant.plans (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    currency public.currency NOT NULL DEFAULT 'USD',
    max_users INTEGER NOT NULL,
    max_services INTEGER NOT NULL,
    max_widgets INTEGER NOT NULL,
    max_resources INTEGER NOT NULL,
    max_api_keys INTEGER NOT NULL,
    features JSONB,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_tenant_plans_code UNIQUE (code)
);
