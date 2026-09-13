INSERT INTO tenant.tenants (organization_id, owner_user_id, ecosystem_id, name, slug, status, plan_id, region, default_timezone, default_locale, max_users, max_services, max_widgets)
SELECT o.id, m.user_id, e.id, o.name, o.slug, 'ACTIVE'::tenant.tenant_status, p.id, 'VN'::tenant.region, o.timezone, u.locale, p.max_users, p.max_services, p.max_widgets
FROM organization.organizations o
JOIN LATERAL (SELECT user_id FROM organization.memberships WHERE organization_id = o.id AND accepted ORDER BY created_at, id LIMIT 1) m ON true
JOIN auth.users u ON u.id = m.user_id
JOIN tenant.plans p ON p.code = 'FREE'
JOIN tenant.ecosystems e ON e.code = 'GENERAL'
WHERE NOT EXISTS (SELECT 1 FROM tenant.tenants t WHERE t.organization_id = o.id)
AND NOT EXISTS (SELECT 1 FROM tenant.tenants t WHERE t.slug = o.slug);
