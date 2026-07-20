INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    ('Create Audit Actor', 'AUDITACTOR', 'CREATE', 'AUDITACTOR.CREATE'),
    ('Read Audit Actor', 'AUDITACTOR', 'READ', 'AUDITACTOR.READ'),

    ('Create Audit Event', 'AUDITEVENT', 'CREATE', 'AUDITEVENT.CREATE'),
    ('Read Audit Event', 'AUDITEVENT', 'READ', 'AUDITEVENT.READ'),

    ('Create Audit Resource', 'AUDITRESOURCE', 'CREATE', 'AUDITRESOURCE.CREATE'),
    ('Read Audit Resource', 'AUDITRESOURCE', 'READ', 'AUDITRESOURCE.READ'),
    
    ('Create Audit Change', 'AUDITCHANGE', 'CREATE', 'AUDITCHANGE.CREATE'),
    ('Read Audit Change', 'AUDITCHANGE', 'READ', 'AUDITCHANGE.READ')
;
