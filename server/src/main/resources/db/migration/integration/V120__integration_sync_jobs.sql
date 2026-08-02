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
