INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    ('Create User', 'USER', 'CREATE', 'USER.CREATE'),
    ('Read User', 'USER', 'READ', 'USER.READ'),
    ('Update User', 'USER', 'UPDATE', 'USER.UPDATE'),
    ('Delete User', 'USER', 'DELETE', 'USER.DELETE'),

    ('Create Role', 'ROLE', 'CREATE', 'ROLE.CREATE'),
    ('Read Role', 'ROLE', 'READ', 'ROLE.READ'),
    ('Update Role', 'ROLE', 'UPDATE', 'ROLE.UPDATE'),
    ('Delete Role', 'ROLE', 'DELETE', 'ROLE.DELETE'),

    ('Create Permission', 'PERMISSION', 'CREATE', 'PERMISSION.CREATE'),
    ('Read Permission', 'PERMISSION', 'READ', 'PERMISSION.READ'),
    ('Update Permission', 'PERMISSION', 'UPDATE', 'PERMISSION.UPDATE'),
    ('Delete Permission', 'PERMISSION', 'DELETE', 'PERMISSION.DELETE'),

    ('Create Account', 'ACCOUNT', 'CREATE', 'ACCOUNT.CREATE'),
    ('Read Account', 'ACCOUNT', 'READ', 'ACCOUNT.READ'),
    ('Update Account', 'ACCOUNT', 'UPDATE', 'ACCOUNT.UPDATE'),
    ('Delete Account', 'ACCOUNT', 'DELETE', 'ACCOUNT.DELETE'),

    ('Create API Key', 'APIKEY', 'CREATE', 'APIKEY.CREATE'),
    ('Read API Key', 'APIKEY', 'READ', 'APIKEY.READ'),
    ('Update API Key', 'APIKEY', 'UPDATE', 'APIKEY.UPDATE'),
    ('Delete API Key', 'APIKEY', 'DELETE', 'APIKEY.DELETE'),
    ('Revoke API Key', 'APIKEY', 'REVOKE', 'APIKEY.REVOKE'),

    ('Create Password', 'PASSWORD', 'CREATE', 'PASSWORD.CREATE'),
    ('Read Password', 'PASSWORD', 'READ', 'PASSWORD.READ'),
    ('Update Password', 'PASSWORD', 'UPDATE', 'PASSWORD.UPDATE'),
    ('Delete Password', 'PASSWORD', 'DELETE', 'PASSWORD.DELETE'),

    ('Read Session', 'SESSION', 'READ', 'SESSION.READ'),
    ('Revoke Session', 'SESSION', 'REVOKE', 'SESSION.REVOKE'),

    ('Create Refresh Token', 'REFRESHTOKEN', 'CREATE', 'REFRESHTOKEN.CREATE'),
    ('Read Refresh Token', 'REFRESHTOKEN', 'READ', 'REFRESHTOKEN.READ'),
    ('Update Refresh Token', 'REFRESHTOKEN', 'UPDATE', 'REFRESHTOKEN.UPDATE'),
    ('Delete Refresh Token', 'REFRESHTOKEN', 'DELETE', 'REFRESHTOKEN.DELETE'),
    ('Revoke Refresh Token', 'REFRESHTOKEN', 'REVOKE', 'REFRESHTOKEN.REVOKE'),

    ('Create User Role', 'USERROLE', 'CREATE', 'USERROLE.CREATE'),
    ('Read User Role', 'USERROLE', 'READ', 'USERROLE.READ'),
    ('Delete User Role', 'USERROLE', 'DELETE', 'USERROLE.DELETE'),

    ('Create Role Permission', 'ROLEPERMISSION', 'CREATE', 'ROLEPERMISSION.CREATE'),
    ('Read Role Permission', 'ROLEPERMISSION', 'READ', 'ROLEPERMISSION.READ'),
    ('Delete Role Permission', 'ROLEPERMISSION', 'DELETE', 'ROLEPERMISSION.DELETE')
;
