-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 2 - Auth - Refresh Tokens
-- =====================================================

CREATE TABLE auth.refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    secret TEXT NOT NULL,
    owner VARCHAR(255),
    user_id BIGINT NOT NULL,
    widget_id BIGINT,
    client VARCHAR(255),
    oauth_client_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,

    CONSTRAINT uq_refresh_tokens_secret UNIQUE (secret),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE
);
