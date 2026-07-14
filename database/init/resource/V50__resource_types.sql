-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 6 - Resource - Resource Types
-- =====================================================

-- created_at/updated_at added for consistency; not in RESOURCE_SCHEMA.md.
CREATE TABLE resource.resource_types (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(100) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_resource_type_organization
        FOREIGN KEY (organization_id)
        REFERENCES organization.organizations(id)
        ON DELETE CASCADE
);
