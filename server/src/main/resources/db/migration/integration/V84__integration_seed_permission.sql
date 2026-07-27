INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    ('Create Destination Calendar', 'DESTINATIONCALENDAR', 'CREATE', 'DESTINATIONCALENDAR.CREATE'),
    ('Read Destination Calendar', 'DESTINATIONCALENDAR', 'READ', 'DESTINATIONCALENDAR.READ'),
    ('Update Destination Calendar', 'DESTINATIONCALENDAR', 'UPDATE', 'DESTINATIONCALENDAR.UPDATE'),
    ('Delete Destination Calendar', 'DESTINATIONCALENDAR', 'DELETE', 'DESTINATIONCALENDAR.DELETE'),
    
    ('Create Destination Sheets', 'DESTINATIONSHEETS', 'CREATE', 'DESTINATIONSHEETS.CREATE'),
    ('Read Destination Sheets', 'DESTINATIONSHEETS', 'READ', 'DESTINATIONSHEETS.READ'),
    ('Update Destination Sheets', 'DESTINATIONSHEETS', 'UPDATE', 'DESTINATIONSHEETS.UPDATE'),
    ('Delete Destination Sheets', 'DESTINATIONSHEETS', 'DELETE', 'DESTINATIONSHEETS.DELETE')
;
