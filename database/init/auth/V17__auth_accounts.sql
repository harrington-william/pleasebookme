-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 2 - Auth - Accounts (OAuth2)
-- =====================================================

CREATE TABLE auth.accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    provider_account_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    access_token TEXT,
    refresh_token TEXT,
    expires_at TIMESTAMPTZ,
    token_type TEXT,
    scope TEXT,
    id_token TEXT,

    CONSTRAINT uq_accounts_provider_account UNIQUE (provider, provider_account_id),
    CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE
);
