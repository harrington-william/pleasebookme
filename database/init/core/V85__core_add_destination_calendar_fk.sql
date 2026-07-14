-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 9 (patch) - Core - Deferred Integration FKs
-- =====================================================

-- Deferred since Phase 5 (V42/V44) - integration.destination_calendars and
-- integration.destination_sheets did not exist until Phase 9. SET NULL on
-- delete: losing a calendar/sheet integration should not destroy the
-- service or booking record.
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
