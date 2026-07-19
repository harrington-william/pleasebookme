-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 5 - Core - Bookings
-- =====================================================

-- `destination_calendar_id` intentionally omitted, same reason as
-- core.services - deferred until Phase 9 (Integration) via ALTER TABLE.

-- `cancelled_by`, `rescheduled_by`, `deleted_by` use ON DELETE SET NULL
-- instead of CASCADE - deleting a staff user should not destroy booking
-- history.
CREATE TABLE core.bookings (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(255),
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    service_id BIGINT NOT NULL,
    location TEXT,
    status core.booking_status NOT NULL DEFAULT 'PENDING',
    paid BOOLEAN NOT NULL DEFAULT false,
    cancelled_by BIGINT,
    cancelation_reason TEXT,
    rejection_reason TEXT,
    rescheduled BOOLEAN NOT NULL DEFAULT false,
    rescheduled_by BIGINT,
    no_show_host BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_bookings_uid UNIQUE (uid),

    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_service FOREIGN KEY (service_id) REFERENCES core.services(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_cancelled_by FOREIGN KEY (cancelled_by) REFERENCES auth.users(id) ON DELETE SET NULL,
    CONSTRAINT fk_booking_rescheduled_by FOREIGN KEY (rescheduled_by) REFERENCES auth.users(id) ON DELETE SET NULL,
    CONSTRAINT fk_booking_deleted_by FOREIGN KEY (deleted_by) REFERENCES auth.users(id) ON DELETE SET NULL
);
