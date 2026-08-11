-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 13 - Indexes - OAuth & Sync
-- =====================================================

CREATE INDEX idx_oauth_connections_user_id ON integration.oauth_connections(user_id);
CREATE INDEX idx_oauth_connections_status ON integration.oauth_connections(status);
CREATE INDEX idx_oauth_connections_provider ON integration.oauth_connections(provider);

CREATE INDEX idx_destination_drives_user_id ON integration.destination_drives(user_id);
CREATE INDEX idx_destination_drives_service_id ON integration.destination_drives(service_id);
CREATE INDEX idx_destination_drives_oauth_connection_id ON integration.destination_drives(oauth_connection_id);

CREATE INDEX idx_destination_calendars_oauth_connection_id ON integration.destination_calendars(oauth_connection_id);
CREATE INDEX idx_destination_sheets_oauth_connection_id ON integration.destination_sheets(oauth_connection_id);

-- The one that matters most: this is the poller's primary query
-- (WHERE status = 'PENDING' AND available_at <= now() ORDER BY
-- available_at ... FOR UPDATE SKIP LOCKED). Composite, in that column
-- order, so it's a single index scan instead of a filter-then-sort.
CREATE INDEX idx_sync_jobs_status_available_at ON integration.sync_jobs(status, available_at);
CREATE INDEX idx_sync_jobs_booking_id ON integration.sync_jobs(booking_id);
CREATE INDEX idx_sync_jobs_oauth_connection_id ON integration.sync_jobs(oauth_connection_id);
