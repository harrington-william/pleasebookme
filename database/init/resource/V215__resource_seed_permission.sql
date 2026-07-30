INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    -- Resource Type
    ('Create Resource Type', 'RESOURCETYPE', 'CREATE', 'RESOURCETYPE.CREATE'),
    ('Read Resource Type', 'RESOURCETYPE', 'READ', 'RESOURCETYPE.READ'),
    ('Update Resource Type', 'RESOURCETYPE', 'UPDATE', 'RESOURCETYPE.UPDATE'),
    ('Delete Resource Type', 'RESOURCETYPE', 'DELETE', 'RESOURCETYPE.DELETE'),

    -- Resource
    ('Create Resource', 'RESOURCE', 'CREATE', 'RESOURCE.CREATE'),
    ('Read Resource', 'RESOURCE', 'READ', 'RESOURCE.READ'),
    ('Update Resource', 'RESOURCE', 'UPDATE', 'RESOURCE.UPDATE'),
    ('Delete Resource', 'RESOURCE', 'DELETE', 'RESOURCE.DELETE'),

    -- Resource Pricing
    ('Create Resource Pricing', 'RESOURCEPRICING', 'CREATE', 'RESOURCEPRICING.CREATE'),
    ('Read Resource Pricing', 'RESOURCEPRICING', 'READ', 'RESOURCEPRICING.READ'),
    ('Update Resource Pricing', 'RESOURCEPRICING', 'UPDATE', 'RESOURCEPRICING.UPDATE'),
    ('Delete Resource Pricing', 'RESOURCEPRICING', 'DELETE', 'RESOURCEPRICING.DELETE'),

    -- Resource Assignment
    ('Create Resource Assignment', 'RESOURCEASSIGNMENT', 'CREATE', 'RESOURCEASSIGNMENT.CREATE'),
    ('Read Resource Assignment', 'RESOURCEASSIGNMENT', 'READ', 'RESOURCEASSIGNMENT.READ'),
    ('Update Resource Assignment', 'RESOURCEASSIGNMENT', 'UPDATE', 'RESOURCEASSIGNMENT.UPDATE'),
    ('Delete Resource Assignment', 'RESOURCEASSIGNMENT', 'DELETE', 'RESOURCEASSIGNMENT.DELETE'),

    -- Resource Calendar
    ('Create Resource Calendar', 'RESOURCECALENDAR', 'CREATE', 'RESOURCECALENDAR.CREATE'),
    ('Read Resource Calendar', 'RESOURCECALENDAR', 'READ', 'RESOURCECALENDAR.READ'),
    ('Update Resource Calendar', 'RESOURCECALENDAR', 'UPDATE', 'RESOURCECALENDAR.UPDATE'),
    ('Delete Resource Calendar', 'RESOURCECALENDAR', 'DELETE', 'RESOURCECALENDAR.DELETE'),

    -- Resource Maintenance
    ('Create Resource Maintenance', 'RESOURCEMAINTENANCE', 'CREATE', 'RESOURCEMAINTENANCE.CREATE'),
    ('Read Resource Maintenance', 'RESOURCEMAINTENANCE', 'READ', 'RESOURCEMAINTENANCE.READ'),
    ('Update Resource Maintenance', 'RESOURCEMAINTENANCE', 'UPDATE', 'RESOURCEMAINTENANCE.UPDATE'),
    ('Delete Resource Maintenance', 'RESOURCEMAINTENANCE', 'DELETE', 'RESOURCEMAINTENANCE.DELETE'),

    -- Resource Attribute
    ('Create Resource Attribute', 'RESOURCEATTRIBUTE', 'CREATE', 'RESOURCEATTRIBUTE.CREATE'),
    ('Read Resource Attribute', 'RESOURCEATTRIBUTE', 'READ', 'RESOURCEATTRIBUTE.READ'),
    ('Update Resource Attribute', 'RESOURCEATTRIBUTE', 'UPDATE', 'RESOURCEATTRIBUTE.UPDATE'),
    ('Delete Resource Attribute', 'RESOURCEATTRIBUTE', 'DELETE', 'RESOURCEATTRIBUTE.DELETE'),

    -- Resource Override
    ('Create Resource Override', 'RESOURCEOVERRIDE', 'CREATE', 'RESOURCEOVERRIDE.CREATE'),
    ('Read Resource Override', 'RESOURCEOVERRIDE', 'READ', 'RESOURCEOVERRIDE.READ'),
    ('Update Resource Override', 'RESOURCEOVERRIDE', 'UPDATE', 'RESOURCEOVERRIDE.UPDATE'),
    ('Delete Resource Override', 'RESOURCEOVERRIDE', 'DELETE', 'RESOURCEOVERRIDE.DELETE')
;
