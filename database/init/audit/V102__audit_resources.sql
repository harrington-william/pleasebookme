-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 11 - Audit - Audit Resources
-- =====================================================

-- `resource_uid` is a loose polymorphic reference (no FK) - resource_type
-- determines which table it points at, no single FK target makes sense.

CREATE TABLE audit.audit_resources (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    resource_type audit.resource_type NOT NULL,
    resource_uid VARCHAR(255) NOT NULL,
    resource_name VARCHAR(255) NOT NULL,
    before_snapshot JSONB,
    after_snapshot JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_audit_resource_event
        FOREIGN KEY (event_id)
        REFERENCES audit.audit_events(id)
        ON DELETE CASCADE
);
