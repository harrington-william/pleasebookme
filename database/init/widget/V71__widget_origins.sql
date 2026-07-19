-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 8 - Widget - Widget Origins
-- =====================================================

-- `created_by` assumed to reference auth.users(id) (who registered the
-- origin) - not specified in WIDGET_SCHEMA.md. ON DELETE SET NULL,
-- matching the attribution-FK convention used elsewhere.

CREATE TABLE widget.widget_origins (
    id BIGSERIAL PRIMARY KEY,
    widget_id BIGINT NOT NULL,
    origin VARCHAR(255) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT false,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_widget_origins_widget_origin UNIQUE (widget_id, origin),

    CONSTRAINT fk_widget_origin_widget FOREIGN KEY (widget_id) REFERENCES widget.widgets(id) ON DELETE CASCADE,
    CONSTRAINT fk_widget_origin_created_by FOREIGN KEY (created_by) REFERENCES auth.users(id) ON DELETE SET NULL
);
