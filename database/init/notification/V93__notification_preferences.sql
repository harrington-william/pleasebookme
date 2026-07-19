-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 10 - Notification - Notification Preferences
-- =====================================================

-- `notification_type` kept as VARCHAR pending a proper enum -
-- NOTIFICATION_SCHEMA.md documents no value set. Unique on
-- (user_id, notification_type) added to prevent duplicate preference
-- rows for the same user and type.

CREATE TABLE notification.notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    email_enabled BOOLEAN NOT NULL DEFAULT false,
    sms_enabled BOOLEAN NOT NULL DEFAULT false,
    push_enabled BOOLEAN NOT NULL DEFAULT false,
    in_app_enabled BOOLEAN NOT NULL DEFAULT false,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_notification_preferences_user_type UNIQUE (user_id, notification_type),

    CONSTRAINT fk_notification_preference_user
        FOREIGN KEY (user_id)
        REFERENCES auth.users(id)
        ON DELETE CASCADE
);
