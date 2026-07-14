-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 10 - Notification - Notifications
-- =====================================================

-- `recipient_uid` added - see NOTIFICATION_SCHEMA.md note, `recipient_type`
-- alone doesn't identify who to notify. Loose polymorphic reference, no
-- FK, since it can point at auth.users, core.attendees, or
-- customer.customers depending on recipient_type.
-- `status` defaulted to 'PENDING' and `priority` to 'NORMAL' - no
-- defaults documented, assumed from context.

CREATE TABLE notification.notifications (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    tenant_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    recipient_type notification.recipient_type NOT NULL,
    recipient_uid VARCHAR(255) NOT NULL,
    template_id BIGINT NOT NULL,
    channel_id BIGINT NOT NULL,
    status notification.notification_status NOT NULL DEFAULT 'PENDING',
    priority notification.notification_priority NOT NULL DEFAULT 'NORMAL',
    subject TEXT,
    content TEXT,
    locale public.locale NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    sent_at TIMESTAMPTZ,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_notifications_uid UNIQUE (uid),

    CONSTRAINT fk_notification_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant.tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_notification_organization
        FOREIGN KEY (organization_id)
        REFERENCES organization.organizations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_notification_template
        FOREIGN KEY (template_id)
        REFERENCES notification.notification_templates(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_notification_channel
        FOREIGN KEY (channel_id)
        REFERENCES notification.notification_channels(id)
        ON DELETE CASCADE
);
