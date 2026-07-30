INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    -- Audit Actor
    ('Create Audit Actor', 'AUDITACTOR', 'CREATE', 'AUDITACTOR.CREATE'),
    ('Read Audit Actor', 'AUDITACTOR', 'READ', 'AUDITACTOR.READ'),

    -- Audit Event
    ('Create Audit Event', 'AUDITEVENT', 'CREATE', 'AUDITEVENT.CREATE'),
    ('Read Audit Event', 'AUDITEVENT', 'READ', 'AUDITEVENT.READ'),

    -- Audit Resource
    ('Create Audit Resource', 'AUDITRESOURCE', 'CREATE', 'AUDITRESOURCE.CREATE'),
    ('Read Audit Resource', 'AUDITRESOURCE', 'READ', 'AUDITRESOURCE.READ'),

    -- Audit Change
    ('Create Audit Change', 'AUDITCHANGE', 'CREATE', 'AUDITCHANGE.CREATE'),
    ('Read Audit Change', 'AUDITCHANGE', 'READ', 'AUDITCHANGE.READ')
;
