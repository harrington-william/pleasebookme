-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 13 - Auth - Remove Unused OAuth Token Columns
-- =====================================================

-- access_token/refresh_token/expires_at/token_type/scope/id_token were
-- carried over from a generic OAuth account-linking template, but the
-- Auth domain only ever needs `provider` + `provider_account_id` to
-- recognize a returning Google-authenticated user. Google's id_token is
-- verified once at login and never persisted. Any token that grants
-- access to a third-party resource (Calendar/Sheets/Drive) belongs to
-- the Integration domain's own oauth_connections table (V2), not
-- here - the Auth domain owns identity, not delegated credentials.
ALTER TABLE auth.accounts DROP COLUMN access_token;
ALTER TABLE auth.accounts DROP COLUMN refresh_token;
ALTER TABLE auth.accounts DROP COLUMN expires_at;
ALTER TABLE auth.accounts DROP COLUMN token_type;
ALTER TABLE auth.accounts DROP COLUMN scope;
ALTER TABLE auth.accounts DROP COLUMN id_token;
