-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 13 - Integration - Add GOOGLE_DRIVE Integration Type
-- =====================================================

-- Kept in its own migration, with no other statement alongside it:
-- Postgres does not allow a newly added enum value to be referenced by
-- the same transaction that added it, and Flyway wraps each migration
-- in a transaction by default. V4 (destination_drives) is the first
-- consumer of this value and runs as a separate migration.
ALTER TYPE integration.integration_type ADD VALUE 'GOOGLE_DRIVE';
