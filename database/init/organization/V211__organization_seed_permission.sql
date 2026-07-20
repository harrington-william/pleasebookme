INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    -- Organization
    ('Create Organization', 'ORGANIZATION', 'CREATE', 'ORGANIZATION.CREATE'),
    ('Read Organization', 'ORGANIZATION', 'READ', 'ORGANIZATION.READ'),
    ('Update Organization', 'ORGANIZATION', 'UPDATE', 'ORGANIZATION.UPDATE'),
    ('Delete Organization', 'ORGANIZATION', 'DELETE', 'ORGANIZATION.DELETE'),

    -- Profile
    ('Create Profile', 'PROFILE', 'CREATE', 'PROFILE.CREATE'),
    ('Read Profile', 'PROFILE', 'READ', 'PROFILE.READ'),
    ('Update Profile', 'PROFILE', 'UPDATE', 'PROFILE.UPDATE'),
    ('Delete Profile', 'PROFILE', 'DELETE', 'PROFILE.DELETE'),

    -- Membership
    ('Create Membership', 'MEMBERSHIP', 'CREATE', 'MEMBERSHIP.CREATE'),
    ('Read Membership', 'MEMBERSHIP', 'READ', 'MEMBERSHIP.READ'),
    ('Update Membership', 'MEMBERSHIP', 'UPDATE', 'MEMBERSHIP.UPDATE'),
    ('Delete Membership', 'MEMBERSHIP', 'DELETE', 'MEMBERSHIP.DELETE'),

    -- Membership Role
    ('Create Membership Role', 'MEMBERSHIPROLE', 'CREATE', 'MEMBERSHIPROLE.CREATE'),
    ('Read Membership Role', 'MEMBERSHIPROLE', 'READ', 'MEMBERSHIPROLE.READ'),
    ('Delete Membership Role', 'MEMBERSHIPROLE', 'DELETE', 'MEMBERSHIPROLE.DELETE'),
    ('Assign Membership Role', 'MEMBERSHIPROLE', 'ASSIGN', 'MEMBERSHIPROLE.ASSIGN'),
    ('Revoke Membership Role', 'MEMBERSHIPROLE', 'REVOKE', 'MEMBERSHIPROLE.REVOKE')
;
