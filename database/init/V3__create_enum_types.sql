-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 1 - Enum Types
-- =====================================================

--------------------------------------------------------
-- Auth
--------------------------------------------------------

CREATE TYPE auth.account_status AS ENUM (
    'ACTIVE',
    'SUSPENDED',
    'LOCKED'
);

CREATE TYPE auth.api_key_status AS ENUM (
    'ACTIVE',
    'REVOKED',
    'EXPIRED'
);

--------------------------------------------------------
-- Core
--------------------------------------------------------

CREATE TYPE core.booking_status AS ENUM (
    'PENDING',
    'ACCEPTED',
    'REJECTED',
    'AWAITING_HOST',
    'CANCELLED'
);

CREATE TYPE core.booking_mode AS ENUM (
    'FIXED',
    'FLEXIBLE',
    'HYBRID'
);

CREATE TYPE core.week_start AS ENUM (
    'MONDAY',
    'SUNDAY'
);

--------------------------------------------------------
-- Tenant
--------------------------------------------------------

CREATE TYPE tenant.tenant_status AS ENUM (
    'ACTIVE',
    'SUSPENDED',
    'TRIAL',
    'PENDING',
    'ARCHIVED'
);

CREATE TYPE tenant.region AS ENUM (
    'AU',
    'UK',
    'US',
    'SG',
    'VN'
);

CREATE TYPE tenant.ecosystem_status AS ENUM (
    'REVIEWING',
    'ACTIVE',
    'SUSPENDED',
    'DISCONTINUED'
);

--------------------------------------------------------
-- Widget
--------------------------------------------------------

CREATE TYPE widget.widget_status AS ENUM (
    'REGISTERING',
    'ACTIVE',
    'DISABLED',
    'REVOKED'
);

CREATE TYPE widget.widget_type AS ENUM (
    'INLINE',
    'POPUP',
    'FULL_PAGE',
    'EMBEDDED'
);

--------------------------------------------------------
-- Notification
--------------------------------------------------------

CREATE TYPE notification.notification_status AS ENUM (
    'PENDING',
    'QUEUED',
    'PROCESSING',
    'SENT',
    'FAILED',
    'CANCELLED',
    'EXPIRED'
);

CREATE TYPE notification.notification_priority AS ENUM (
    'LOW',
    'NORMAL',
    'HIGH',
    'CRITICAL'
);

CREATE TYPE notification.recipient_type AS ENUM (
    'USER',
    'ATTENDEE',
    'CUSTOMER',
    'SYSTEM'
);

--------------------------------------------------------
-- Audit
--------------------------------------------------------

CREATE TYPE audit.audit_action AS ENUM (
    'CREATED',
    'UPDATED',
    'DELETED',
    'CANCELLED',
    'ACCEPTED',
    'REJECTED',
    'PENDING',
    'AWAITING_HOST',
    'RESCHEDULED',
    'LOCATION_CHANGED',
    'NO_SHOW_UPDATED',
    'REGISTERED',
    'GRANTED',
    'REVOKED',
    'LOGIN',
    'LOGOUT',
    'SYNCED',
    'FAILED',
    'STARTED',
    'COMPLETED',
    'EXPIRED'
);

CREATE TYPE audit.audit_actor_type AS ENUM (
    'GUEST',
    'ATTENDEE',
    'SYSTEM',
    'WIDGET',
    'API_KEY',
    'WEBHOOK',
    'INTEGRATION'
);

CREATE TYPE audit.event_domain AS ENUM (
    'AUTH',
    'CORE',
    'RESOURCE',
    'TENANT',
    'WIDGET',
    'INTEGRATION',
    'NOTIFICATION',
    'BILLING',
    'SYSTEM',
    'ORGANIZATION'
);

CREATE TYPE audit.event_type AS ENUM (
    'USER',
    'ROLE',
    'PERMISSION',
    'SERVICE',
    'RESOURCE',
    'WIDGET',
    'MEMBERSHIP',
    'CALENDAR',
    'TENANT'
);

CREATE TYPE audit.severity AS ENUM (
    'INFO',
    'WARNING',
    'ERROR',
    'SECURITY',
    'CRITICAL'
);

CREATE TYPE audit.audit_status AS ENUM (
    'SUCCESS',
    'FAILED',
    'PARTIAL'
);

CREATE TYPE audit.resource_type AS ENUM (
    'BOOKING',
    'SERVICE',
    'RESOURCE',
    'USER',
    'ROLE',
    'PERMISSION',
    'TENANT',
    'WIDGET',
    'WEBHOOK',
    'CALENDAR',
    'PROFILE',
    'MEMBERSHIP',
    'NOTIFICATION',
    'INTEGRATION'
);

CREATE TYPE audit.event_source AS ENUM (
    'WEB',
    'API',
    'WIDGET',
    'SYSTEM',
    'WEBHOOK',
    'GOOGLE_CALENDAR'
);