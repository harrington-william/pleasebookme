-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 12 - Indexes - Organization
-- =====================================================

CREATE INDEX idx_profiles_user_id ON organization.profiles(user_id);
CREATE INDEX idx_profiles_organization_id ON organization.profiles(organization_id);

CREATE INDEX idx_memberships_organization_id ON organization.memberships(organization_id);
CREATE INDEX idx_memberships_user_id ON organization.memberships(user_id);
CREATE INDEX idx_memberships_accepted ON organization.memberships(accepted);
