-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 2 - Auth - Permissions
-- =====================================================

CREATE TABLE auth.permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_permissions_slug UNIQUE (slug)
);
