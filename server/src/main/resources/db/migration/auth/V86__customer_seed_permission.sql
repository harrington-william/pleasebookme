INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    ('Create Customer', 'CUSTOMER', 'CREATE', 'CUSTOMER.CREATE'),
    ('Read Customer', 'CUSTOMER', 'READ', 'CUSTOMER.READ'),
    ('Update Customer', 'CUSTOMER', 'UPDATE', 'CUSTOMER.UPDATE'),
    ('Delete Customer', 'CUSTOMER', 'DELETE', 'CUSTOMER.DELETE'),

    ('Create Customer Note', 'CUSTOMERNOTE', 'CREATE', 'CUSTOMERNOTE.CREATE'),
    ('Read Customer Note', 'CUSTOMERNOTE', 'READ', 'CUSTOMERNOTE.READ'),
    ('Update Customer Note', 'CUSTOMERNOTE', 'UPDATE', 'CUSTOMERNOTE.UPDATE'),
    ('Delete Customer Note', 'CUSTOMERNOTE', 'DELETE', 'CUSTOMERNOTE.DELETE'),

    ('Create Customer Activity', 'CUSTOMERACTIVITY', 'CREATE', 'CUSTOMERACTIVITY.CREATE'),
    ('Read Customer Activity', 'CUSTOMERACTIVITY', 'READ', 'CUSTOMERACTIVITY.READ'),
    ('Update Customer Activity', 'CUSTOMERACTIVITY', 'UPDATE', 'CUSTOMERACTIVITY.UPDATE'),
    ('Delete Customer Activity', 'CUSTOMERACTIVITY', 'DELETE', 'CUSTOMERACTIVITY.DELETE'),

    ('Create Customer Tag', 'CUSTOMERTAG', 'CREATE', 'CUSTOMERTAG.CREATE'),
    ('Read Customer Tag', 'CUSTOMERTAG', 'READ', 'CUSTOMERTAG.READ'),
    ('Update Customer Tag', 'CUSTOMERTAG', 'UPDATE', 'CUSTOMERTAG.UPDATE'),
    ('Delete Customer Tag', 'CUSTOMERTAG', 'DELETE', 'CUSTOMERTAG.DELETE'),

    ('Create Customer Source', 'CUSTOMERSOURCE', 'CREATE', 'CUSTOMERSOURCE.CREATE'),
    ('Read Customer Source', 'CUSTOMERSOURCE', 'READ', 'CUSTOMERSOURCE.READ'),
    ('Update Customer Source', 'CUSTOMERSOURCE', 'UPDATE', 'CUSTOMERSOURCE.UPDATE'),
    ('Delete Customer Source', 'CUSTOMERSOURCE', 'DELETE', 'CUSTOMERSOURCE.DELETE')
;
