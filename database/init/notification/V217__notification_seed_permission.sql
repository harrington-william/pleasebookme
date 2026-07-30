INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    -- Notification Channel
    ('Create Notification Channel', 'NOTIFICATIONCHANNEL', 'CREATE', 'NOTIFICATIONCHANNEL.CREATE'),
    ('Read Notification Channel', 'NOTIFICATIONCHANNEL', 'READ', 'NOTIFICATIONCHANNEL.READ'),
    ('Update Notification Channel', 'NOTIFICATIONCHANNEL', 'UPDATE', 'NOTIFICATIONCHANNEL.UPDATE'),
    ('Delete Notification Channel', 'NOTIFICATIONCHANNEL', 'DELETE', 'NOTIFICATIONCHANNEL.DELETE'),

    -- Notification Template
    ('Create Notification Template', 'NOTIFICATIONTEMPLATE', 'CREATE', 'NOTIFICATIONTEMPLATE.CREATE'),
    ('Read Notification Template', 'NOTIFICATIONTEMPLATE', 'READ', 'NOTIFICATIONTEMPLATE.READ'),
    ('Update Notification Template', 'NOTIFICATIONTEMPLATE', 'UPDATE', 'NOTIFICATIONTEMPLATE.UPDATE'),
    ('Delete Notification Template', 'NOTIFICATIONTEMPLATE', 'DELETE', 'NOTIFICATIONTEMPLATE.DELETE'),

    -- Notification
    ('Create Notification', 'NOTIFICATION', 'CREATE', 'NOTIFICATION.CREATE'),
    ('Read Notification', 'NOTIFICATION', 'READ', 'NOTIFICATION.READ'),
    ('Update Notification', 'NOTIFICATION', 'UPDATE', 'NOTIFICATION.UPDATE'),
    ('Delete Notification', 'NOTIFICATION', 'DELETE', 'NOTIFICATION.DELETE'),

    -- Notification Preference
    ('Create Notification Preference', 'NOTIFICATIONPREFERENCE', 'CREATE', 'NOTIFICATIONPREFERENCE.CREATE'),
    ('Read Notification Preference', 'NOTIFICATIONPREFERENCE', 'READ', 'NOTIFICATIONPREFERENCE.READ'),
    ('Update Notification Preference', 'NOTIFICATIONPREFERENCE', 'UPDATE', 'NOTIFICATIONPREFERENCE.UPDATE'),
    ('Delete Notification Preference', 'NOTIFICATIONPREFERENCE', 'DELETE', 'NOTIFICATIONPREFERENCE.DELETE'),

    -- Notification Queue
    ('Create Notification Queue', 'NOTIFICATIONQUEUE', 'CREATE', 'NOTIFICATIONQUEUE.CREATE'),
    ('Read Notification Queue', 'NOTIFICATIONQUEUE', 'READ', 'NOTIFICATIONQUEUE.READ'),
    ('Update Notification Queue', 'NOTIFICATIONQUEUE', 'UPDATE', 'NOTIFICATIONQUEUE.UPDATE'),
    ('Delete Notification Queue', 'NOTIFICATIONQUEUE', 'DELETE', 'NOTIFICATIONQUEUE.DELETE'),

    -- Notification Delivery
    ('Create Notification Delivery', 'NOTIFICATIONDELIVERY', 'CREATE', 'NOTIFICATIONDELIVERY.CREATE'),
    ('Read Notification Delivery', 'NOTIFICATIONDELIVERY', 'READ', 'NOTIFICATIONDELIVERY.READ'),
    ('Update Notification Delivery', 'NOTIFICATIONDELIVERY', 'UPDATE', 'NOTIFICATIONDELIVERY.UPDATE'),
    ('Delete Notification Delivery', 'NOTIFICATIONDELIVERY', 'DELETE', 'NOTIFICATIONDELIVERY.DELETE')
;
