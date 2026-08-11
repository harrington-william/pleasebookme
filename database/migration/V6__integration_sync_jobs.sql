-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 13 - Integration - Sync Jobs
-- =====================================================

-- Durable outbox for outbound Google API work (Calendar push, Sheets
-- append, Drive backup), triggered by a booking event. Modeled directly
-- on notification.notification_queue's shape (status/attempts/
-- available_at/last_attempt_at) - same pattern already proven in this
-- codebase, just not yet applied to Integration. This table is what
-- lets Booking's write path stay fast and Google-outage-proof: it only
-- ever inserts a row here, never calls Google inline, and a separate
-- poller performs the actual external call out of band.
--
-- Scoped to core.bookings only, not a generic event table - broader
-- event sourcing arrives with Kafka in Platform v2.0. This is
-- intentionally a V1, booking-scoped outbox, not an event-bus
-- imitation.
CREATE TYPE integration.sync_job_type AS ENUM (
    'CALENDAR_SYNC',
    'SHEET_SYNC',
    'DRIVE_BACKUP'
);

CREATE TYPE integration.sync_job_status AS ENUM (
    'PENDING',
    'PROCESSING',
    'SUCCEEDED',
    'FAILED',
    'DEAD_LETTER'
);

-- max_attempts bounds retries so a permanently-broken connection (e.g.
-- revoked) can't retry forever and quietly consume worker capacity -
-- it flips to DEAD_LETTER once exhausted, surfaced to an operator
-- rather than retried silently.
CREATE TABLE integration.sync_jobs (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    job_type integration.sync_job_type NOT NULL,
    booking_id BIGINT NOT NULL,
    oauth_connection_id BIGINT NOT NULL,
    status integration.sync_job_status NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    available_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_attempt_at TIMESTAMPTZ,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_sync_jobs_uid UNIQUE (uid),

    CONSTRAINT fk_sync_job_booking
        FOREIGN KEY (booking_id)
        REFERENCES core.bookings(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sync_job_oauth_connection
        FOREIGN KEY (oauth_connection_id)
        REFERENCES integration.oauth_connections(id)
        ON DELETE CASCADE
);
