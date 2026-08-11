-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 13 - Integration - Destination Drives
-- =====================================================

-- Same shape as destination_calendars (V80) / destination_sheets (V81):
-- per-service record of which external target booking backups sync
-- to. oauth_connection_id is included from creation (unlike the
-- retrofit needed on the two older destination tables in V5) since
-- this table has no existing rows to migrate - every destination row's
-- only reason to exist is to be synced through a specific connection,
-- so the FK is mandatory here, not optional.
CREATE TABLE integration.destination_drives (
    id BIGSERIAL PRIMARY KEY,
    integration_type integration.integration_type NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    oauth_connection_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_destination_drive_user
        FOREIGN KEY (user_id)
        REFERENCES auth.users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_destination_drive_service
        FOREIGN KEY (service_id)
        REFERENCES core.services(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_destination_drive_oauth_connection
        FOREIGN KEY (oauth_connection_id)
        REFERENCES integration.oauth_connections(id)
        ON DELETE CASCADE
);
