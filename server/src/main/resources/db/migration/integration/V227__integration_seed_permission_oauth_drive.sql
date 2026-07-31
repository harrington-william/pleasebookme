INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    ('Create OAuth Connection', 'OAUTHCONNECTION', 'CREATE', 'OAUTHCONNECTION.CREATE'),
    ('Read OAuth Connection', 'OAUTHCONNECTION', 'READ', 'OAUTHCONNECTION.READ'),
    ('Update OAuth Connection', 'OAUTHCONNECTION', 'UPDATE', 'OAUTHCONNECTION.UPDATE'),
    ('Delete OAuth Connection', 'OAUTHCONNECTION', 'DELETE', 'OAUTHCONNECTION.DELETE'),

    ('Create Destination Drive', 'DESTINATIONDRIVE', 'CREATE', 'DESTINATIONDRIVE.CREATE'),
    ('Read Destination Drive', 'DESTINATIONDRIVE', 'READ', 'DESTINATIONDRIVE.READ'),
    ('Update Destination Drive', 'DESTINATIONDRIVE', 'UPDATE', 'DESTINATIONDRIVE.UPDATE'),
    ('Delete Destination Drive', 'DESTINATIONDRIVE', 'DELETE', 'DESTINATIONDRIVE.DELETE')
;
