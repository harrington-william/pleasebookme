-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 4 - Tenant - Tenants
-- =====================================================

CREATE TABLE tenant.tenants (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    organization_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    ecosystem_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    status tenant.tenant_status NOT NULL,
    plan_id BIGINT NOT NULL,
    region tenant.region NOT NULL,
    default_timezone VARCHAR(100) NOT NULL DEFAULT 'Australia/Sydney',
    default_locale public.locale NOT NULL DEFAULT 'en',
    max_users INTEGER NOT NULL,
    max_services INTEGER NOT NULL,
    max_widgets INTEGER NOT NULL,
    settings JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_tenants_uid UNIQUE (uid),
    CONSTRAINT uq_tenants_slug UNIQUE (slug),

    CONSTRAINT fk_tenant_organization FOREIGN KEY (organization_id) REFERENCES organization.organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_tenant_owner FOREIGN KEY (owner_user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_tenant_ecosystem FOREIGN KEY (ecosystem_id) REFERENCES tenant.ecosystems(id) ON DELETE CASCADE,
    CONSTRAINT fk_tenant_plan FOREIGN KEY (plan_id) REFERENCES tenant.plans(id) ON DELETE CASCADE
);
