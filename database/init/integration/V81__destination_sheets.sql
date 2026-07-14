-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 9 - Integration - Destination Sheets
-- =====================================================

-- Same `integration` -> `integration_type` rename as destination_calendars,
-- reusing the same integration.integration_type enum created in V80.

CREATE TABLE integration.destination_sheets (
    id BIGSERIAL PRIMARY KEY,
    integration_type integration.integration_type NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_destination_sheet_user
        FOREIGN KEY (user_id)
        REFERENCES auth.users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_destination_sheet_service
        FOREIGN KEY (service_id)
        REFERENCES core.services(id)
        ON DELETE CASCADE
);
