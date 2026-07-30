INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    -- Widget
    ('Create Widget', 'WIDGET', 'CREATE', 'WIDGET.CREATE'),
    ('Read Widget', 'WIDGET', 'READ', 'WIDGET.READ'),
    ('Update Widget', 'WIDGET', 'UPDATE', 'WIDGET.UPDATE'),
    ('Delete Widget', 'WIDGET', 'DELETE', 'WIDGET.DELETE'),

    -- Widget Origin
    ('Create Widget Origin', 'WIDGETORIGIN', 'CREATE', 'WIDGETORIGIN.CREATE'),
    ('Read Widget Origin', 'WIDGETORIGIN', 'READ', 'WIDGETORIGIN.READ'),
    ('Update Widget Origin', 'WIDGETORIGIN', 'UPDATE', 'WIDGETORIGIN.UPDATE'),
    ('Delete Widget Origin', 'WIDGETORIGIN', 'DELETE', 'WIDGETORIGIN.DELETE')
;
