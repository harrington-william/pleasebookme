INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    -- Tenant
    ('Create Tenant', 'TENANT', 'CREATE', 'TENANT.CREATE'),
    ('Read Tenant', 'TENANT', 'READ', 'TENANT.READ'),
    ('Update Tenant', 'TENANT', 'UPDATE', 'TENANT.UPDATE'),
    ('Delete Tenant', 'TENANT', 'DELETE', 'TENANT.DELETE'),

    -- Ecosystem
    ('Create Ecosystem', 'ECOSYSTEM', 'CREATE', 'ECOSYSTEM.CREATE'),
    ('Read Ecosystem', 'ECOSYSTEM', 'READ', 'ECOSYSTEM.READ'),
    ('Update Ecosystem', 'ECOSYSTEM', 'UPDATE', 'ECOSYSTEM.UPDATE'),
    ('Delete Ecosystem', 'ECOSYSTEM', 'DELETE', 'ECOSYSTEM.DELETE'),

    -- Tenant Plan
    ('Create Tenant Plan', 'TENANTPLAN', 'CREATE', 'TENANTPLAN.CREATE'),
    ('Read Tenant Plan', 'TENANTPLAN', 'READ', 'TENANTPLAN.READ'),
    ('Update Tenant Plan', 'TENANTPLAN', 'UPDATE', 'TENANTPLAN.UPDATE'),
    ('Delete Tenant Plan', 'TENANTPLAN', 'DELETE', 'TENANTPLAN.DELETE'),

    -- Tenant Domain
    ('Create Tenant Domain', 'TENANTDOMAIN', 'CREATE', 'TENANTDOMAIN.CREATE'),
    ('Read Tenant Domain', 'TENANTDOMAIN', 'READ', 'TENANTDOMAIN.READ'),
    ('Update Tenant Domain', 'TENANTDOMAIN', 'UPDATE', 'TENANTDOMAIN.UPDATE'),
    ('Delete Tenant Domain', 'TENANTDOMAIN', 'DELETE', 'TENANTDOMAIN.DELETE')
;
