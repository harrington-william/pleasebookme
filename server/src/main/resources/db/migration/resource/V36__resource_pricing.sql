CREATE TABLE resource.resource_pricing (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    currency public.currency NOT NULL DEFAULT 'USD',
    effective_from TIMESTAMPTZ NOT NULL,
    effective_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_resource_pricing_resource FOREIGN KEY (resource_id) REFERENCES resource.resources(id) ON DELETE CASCADE
);
