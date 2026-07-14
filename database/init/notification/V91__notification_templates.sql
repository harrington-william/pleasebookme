-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 10 - Notification - Notification Templates
-- =====================================================

-- `channel` kept as VARCHAR matching notification_channels.code (e.g.
-- 'EMAIL'), not a hard FK - NOTIFICATION_SCHEMA.md names it differently
-- from Notifications.channel_id, which does FK to notification_channels.
-- `enabled` defaulted to false (no default documented). Unique on
-- (tenant_id, code): since tenant_id is nullable, this does not stop two
-- platform-wide (tenant_id IS NULL) templates from sharing a code -
-- Postgres treats NULLs as distinct for uniqueness purposes.

CREATE TABLE notification.notification_templates (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    subject_template TEXT,
    body_template TEXT NOT NULL,
    locale public.locale NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT false,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_notification_templates_tenant_code UNIQUE (tenant_id, code),

    CONSTRAINT fk_notification_template_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant.tenants(id)
        ON DELETE CASCADE
);
