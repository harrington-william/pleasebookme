ALTER TABLE core.services ADD COLUMN destination_calendar_id BIGINT;

ALTER TABLE core.services ADD CONSTRAINT fk_service_destination_calendar
    FOREIGN KEY (destination_calendar_id)
    REFERENCES integration.destination_calendars(id)
    ON DELETE SET NULL;

ALTER TABLE core.bookings ADD COLUMN destination_calendar_id BIGINT;

ALTER TABLE core.bookings ADD CONSTRAINT fk_booking_destination_calendar
    FOREIGN KEY (destination_calendar_id)
    REFERENCES integration.destination_calendars(id)
    ON DELETE SET NULL;

ALTER TABLE core.bookings ADD COLUMN destination_sheets_id BIGINT;

ALTER TABLE core.bookings ADD CONSTRAINT fk_booking_destination_sheets
    FOREIGN KEY (destination_sheets_id)
    REFERENCES integration.destination_sheets(id)
    ON DELETE SET NULL;
