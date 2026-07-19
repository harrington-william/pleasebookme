-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 8 - Widget - Widgets
-- =====================================================

-- `origin_validation` assumed BOOLEAN, defaulting to enforced (true) -
-- type/default not specified in WIDGET_SCHEMA.md. `last_used_at` made
-- nullable despite no "?" - a freshly created widget has no last-used
-- timestamp yet.

CREATE TABLE widget.widgets (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    tenant_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    status widget.widget_status NOT NULL DEFAULT 'REGISTERING',
    type widget.widget_type NOT NULL DEFAULT 'EMBEDDED',
    origin_validation BOOLEAN NOT NULL DEFAULT false,
    public_key TEXT NOT NULL,
    secret_key TEXT NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_widgets_uid UNIQUE (uid),
    CONSTRAINT uq_widgets_public_key UNIQUE (public_key),

    CONSTRAINT fk_widget_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant.tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_widget_service
        FOREIGN KEY (service_id)
        REFERENCES core.services(id)
        ON DELETE CASCADE
);
