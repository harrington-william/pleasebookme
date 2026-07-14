-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 5 - Core - Out Of Office
-- =====================================================

CREATE TABLE core.out_of_office (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    notes TEXT,
    show_note_publicly BOOLEAN NOT NULL DEFAULT false,
    user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_out_of_office_uid UNIQUE (uid),

    CONSTRAINT fk_out_of_office_user
        FOREIGN KEY (user_id)
        REFERENCES auth.users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_out_of_office_to_user
        FOREIGN KEY (to_user_id)
        REFERENCES auth.users(id)
        ON DELETE CASCADE
);
