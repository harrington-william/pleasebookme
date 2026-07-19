-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 9 - Integration - Destination Calendars
-- =====================================================

-- Field renamed from `integration` (ambiguous) to `integration_type`.
-- Its value set is fully documented ("Integration Type (Enum)"), so it
-- gets a real Postgres enum instead of a VARCHAR placeholder.
CREATE TYPE integration.integration_type AS ENUM (
    'GOOGLE_CALENDAR',
    'GOOGLE_SHEET',
    'OUTLOOK'
);

CREATE TABLE integration.destination_calendars (
    id BIGSERIAL PRIMARY KEY,
    integration_type integration.integration_type NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_destination_calendar_user
        FOREIGN KEY (user_id)
        REFERENCES auth.users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_destination_calendar_service
        FOREIGN KEY (service_id)
        REFERENCES core.services(id)
        ON DELETE CASCADE
);
