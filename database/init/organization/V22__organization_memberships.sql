-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 3 - Organization - Memberships
-- =====================================================

CREATE TABLE organization.memberships (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    accepted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_memberships_user_organization UNIQUE (user_id, organization_id),
    
    CONSTRAINT fk_membership_organization FOREIGN KEY (organization_id) REFERENCES organization.organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_membership_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE
);
