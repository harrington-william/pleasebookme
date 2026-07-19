-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 5 - Core - Services
-- =====================================================

-- `destination_calendar_id` intentionally omitted. It references
-- integration.destination_calendars, which does not exist until Phase 9.
-- Added later via a deferred ALTER TABLE patch, per DATABASE_INIT_STRATEGY.md.

-- `period_type` kept as VARCHAR pending a proper enum - only 'UNLIMITED'
-- is documented as a value in CORE_SCHEMA.md, the rest of the value set
-- is undefined.
CREATE TABLE core.services (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    interface_language public.locale NOT NULL DEFAULT 'en',
    location TEXT,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    period_type VARCHAR(50) NOT NULL DEFAULT 'UNLIMITED',
    timezone VARCHAR(100) NOT NULL DEFAULT 'Australia/Sydney',
    min_price NUMERIC(10, 2),
    max_price NUMERIC(10, 2),
    currency public.currency NOT NULL DEFAULT 'USD',
    requires_confirmation BOOLEAN NOT NULL DEFAULT false,
    disable_cancelling BOOLEAN NOT NULL DEFAULT false,
    disable_rescheduling BOOLEAN NOT NULL DEFAULT false,
    success_redirect_url TEXT,
    is_instant_service BOOLEAN NOT NULL DEFAULT false,
    max_active_booking_per_booker INTEGER,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_services_organization_slug UNIQUE (organization_id, slug),
    
    CONSTRAINT fk_service_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_service_profile FOREIGN KEY (profile_id) REFERENCES organization.profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_service_organization FOREIGN KEY (organization_id) REFERENCES organization.organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_service_schedule FOREIGN KEY (schedule_id) REFERENCES core.schedules(id) ON DELETE CASCADE
);
