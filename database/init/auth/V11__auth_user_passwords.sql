-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 2 - Auth - User Passwords
-- =====================================================

-- `raw` intentionally stores the plaintext password. Platform V1.0.0 is
-- privately onboarded: every account's password is set by the operator,
-- not chosen by the account holder through self-service. This column is
-- removed once the platform opens up to self-service SaaS signup.
CREATE TABLE auth.user_passwords (
    user_id BIGINT PRIMARY KEY,
    raw TEXT NOT NULL,
    hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_user_password_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE
);
