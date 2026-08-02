INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    ('Create Sync Job', 'SYNCJOB', 'CREATE', 'SYNCJOB.CREATE'),
    ('Read Sync Job', 'SYNCJOB', 'READ', 'SYNCJOB.READ'),
    ('Update Sync Job', 'SYNCJOB', 'UPDATE', 'SYNCJOB.UPDATE'),
    ('Delete Sync Job', 'SYNCJOB', 'DELETE', 'SYNCJOB.DELETE')
;
