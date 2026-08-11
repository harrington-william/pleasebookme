-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 13 - Integration - Bind Existing Destinations to OAuth Connections
-- =====================================================

-- destination_calendars (V80) and destination_sheets (V81) predate
-- oauth_connections (V2) and were created with no way to say which
-- credential actually performs the sync. Added nullable here, same
-- caution V85 used adding destination_calendar_id/destination_sheets_id
-- onto core.services/core.bookings - an ALTER TABLE ADD COLUMN NOT NULL
-- against a table that may already hold rows needs a backfill step this
-- migration does not perform. The application is expected to always
-- populate it going forward; tightening to NOT NULL is a follow-up
-- migration once any existing rows are backfilled.
ALTER TABLE integration.destination_calendars ADD COLUMN oauth_connection_id BIGINT;

ALTER TABLE integration.destination_calendars ADD CONSTRAINT fk_destination_calendar_oauth_connection
    FOREIGN KEY (oauth_connection_id)
    REFERENCES integration.oauth_connections(id)
    ON DELETE CASCADE;

ALTER TABLE integration.destination_sheets ADD COLUMN oauth_connection_id BIGINT;

ALTER TABLE integration.destination_sheets ADD CONSTRAINT fk_destination_sheet_oauth_connection
    FOREIGN KEY (oauth_connection_id)
    REFERENCES integration.oauth_connections(id)
    ON DELETE CASCADE;
