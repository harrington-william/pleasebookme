ALTER TABLE tenant.tenants
ADD CONSTRAINT uq_tenants_organization UNIQUE (organization_id);

DROP INDEX tenant.idx_tenants_organization_id;
