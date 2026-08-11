-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 13 - Auth - Remove Plaintext Password Column
-- =====================================================

-- `raw` was an accepted, time-boxed exception for the privately-onboarded
-- V1.0.0 phase (see V11), not a permanent design. The platform is moving
-- to self-service signup with Google Sign-In promoted as the primary
-- entry point, which ends the operator-sets-every-password assumption
-- that justified storing plaintext in the first place. `hash` alone is
-- sufficient for the password-login path going forward. This table's
-- shared primary key (user_id) already makes a password row optional
-- per user - a Google-only account simply has no row here.
ALTER TABLE auth.user_passwords DROP COLUMN raw;
