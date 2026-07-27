INSERT INTO auth.role_permissions(role_id, permission_id)
SELECT
    r.id,
    p.id
FROM auth.roles AS r
CROSS JOIN auth.permissions AS p
WHERE r.name = 'PLATFORM_OWNER';