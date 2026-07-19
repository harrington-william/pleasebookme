-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 5 - Core - Availabilities
-- =====================================================

-- `days` assumed as an array of weekday numbers (0-6). Not specified in
-- CORE_SCHEMA.md; revisit if a different representation was intended.
CREATE TABLE core.availabilities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    days INTEGER[] NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_availability_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_availability_schedule FOREIGN KEY (schedule_id) REFERENCES core.schedules(id) ON DELETE CASCADE
);
