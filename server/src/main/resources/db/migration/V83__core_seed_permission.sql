INSERT INTO auth.permissions(name, resource, action, slug)
VALUES
    ('Create Schedule', 'SCHEDULE', 'CREATE', 'SCHEDULE.CREATE'),
    ('Read Schedule', 'SCHEDULE', 'READ', 'SCHEDULE.READ'),
    ('Update Schedule', 'SCHEDULE', 'UPDATE', 'SCHEDULE.UPDATE'),
    ('Delete Schedule', 'SCHEDULE', 'DELETE', 'SCHEDULE.DELETE'),

    ('Create Availability', 'AVAILABILITY', 'CREATE', 'AVAILABILITY.CREATE'),
    ('Read Availability', 'AVAILABILITY', 'READ', 'AVAILABILITY.READ'),
    ('Update Availability', 'AVAILABILITY', 'UPDATE', 'AVAILABILITY.UPDATE'),
    ('Delete Availability', 'AVAILABILITY', 'DELETE', 'AVAILABILITY.DELETE'),

    ('Create Service', 'SERVICE', 'CREATE', 'SERVICE.CREATE'),
    ('Read Service', 'SERVICE', 'READ', 'SERVICE.READ'),
    ('Update Service', 'SERVICE', 'UPDATE', 'SERVICE.UPDATE'),
    ('Delete Service', 'SERVICE', 'DELETE', 'SERVICE.DELETE'),

    ('Create Booking Policy', 'BOOKINGPOLICY', 'CREATE', 'BOOKINGPOLICY.CREATE'),
    ('Read Booking Policy', 'BOOKINGPOLICY', 'READ', 'BOOKINGPOLICY.READ'),
    ('Update Booking Policy', 'BOOKINGPOLICY', 'UPDATE', 'BOOKINGPOLICY.UPDATE'),
    ('Delete Booking Policy', 'BOOKINGPOLICY', 'DELETE', 'BOOKINGPOLICY.DELETE'),

    ('Create Booking', 'BOOKING', 'CREATE', 'BOOKING.CREATE'),
    ('Read Booking', 'BOOKING', 'READ', 'BOOKING.READ'),
    ('Update Booking', 'BOOKING', 'UPDATE', 'BOOKING.UPDATE'),
    ('Delete Booking', 'BOOKING', 'DELETE', 'BOOKING.DELETE'),
    ('Cancel Booking', 'BOOKING', 'CANCEL', 'BOOKING.CANCEL'),
    ('Reject Booking', 'BOOKING', 'REJECT', 'BOOKING.REJECT'),

    ('Create Attendee', 'ATTENDEE', 'CREATE', 'ATTENDEE.CREATE'),
    ('Read Attendee', 'ATTENDEE', 'READ', 'ATTENDEE.READ'),
    ('Update Attendee', 'ATTENDEE', 'UPDATE', 'ATTENDEE.UPDATE'),
    ('Delete Attendee', 'ATTENDEE', 'DELETE', 'ATTENDEE.DELETE'),

    ('Create Selected Slot', 'SELECTEDSLOT', 'CREATE', 'SELECTEDSLOT.CREATE'),
    ('Read Selected Slot', 'SELECTEDSLOT', 'READ', 'SELECTEDSLOT.READ'),
    ('Update Selected Slot', 'SELECTEDSLOT', 'UPDATE', 'SELECTEDSLOT.UPDATE'),
    ('Delete Selected Slot', 'SELECTEDSLOT', 'DELETE', 'SELECTEDSLOT.DELETE'),

    ('Create Out Of Office', 'OUTOFOFFICE', 'CREATE', 'OUTOFOFFICE.CREATE'),
    ('Read Out Of Office', 'OUTOFOFFICE', 'READ', 'OUTOFOFFICE.READ'),
    ('Update Out Of Office', 'OUTOFOFFICE', 'UPDATE', 'OUTOFOFFICE.UPDATE'),
    ('Delete Out Of Office', 'OUTOFOFFICE', 'DELETE', 'OUTOFOFFICE.DELETE')
;
