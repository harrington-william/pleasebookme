-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 6 - Resource - Resource Pricing
-- =====================================================

-- `effective_until` made nullable, despite no "?" in RESOURCE_SCHEMA.md -
-- a currently-active price has no known end date yet; forcing NOT NULL
-- would make it impossible to set today's price without inventing a fake
-- future end date. `created_at` added since pricing rows are effectively
-- an append-only history, not documented originally.
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
