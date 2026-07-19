-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 3 - Organization - Membership Roles
-- =====================================================

CREATE TABLE organization.membership_roles (
    membership_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (membership_id, role_id),

    CONSTRAINT fk_membership_role_membership FOREIGN KEY (membership_id) REFERENCES organization.memberships(id) ON DELETE CASCADE,
    CONSTRAINT fk_membership_role_role FOREIGN KEY (role_id) REFERENCES auth.roles(id) ON DELETE CASCADE
);
