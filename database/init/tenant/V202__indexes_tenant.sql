-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 12 - Indexes - Tenant
-- =====================================================

CREATE INDEX idx_tenants_owner_user_id ON tenant.tenants(owner_user_id);
CREATE INDEX idx_tenants_organization_id ON tenant.tenants(organization_id);
