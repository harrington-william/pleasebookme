-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 6 - Resource - Resource Availability Overrides
-- =====================================================

-- `id` added as a surrogate PK - not in RESOURCE_SCHEMA.md, but there is
-- no natural unique key across (resource_id, start_time, end_time).

CREATE TABLE resource.resource_overrides (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_resource_override_resource
        FOREIGN KEY (resource_id)
        REFERENCES resource.resources(id)
        ON DELETE CASCADE
);
