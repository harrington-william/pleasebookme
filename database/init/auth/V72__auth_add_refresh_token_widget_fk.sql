-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 8 (patch) - Auth - Deferred Widget FK
-- =====================================================

-- Deferred since Phase 2 - widget.widgets did not exist until Phase 8.
ALTER TABLE auth.refresh_tokens ADD CONSTRAINT fk_refresh_token_widget
    FOREIGN KEY (widget_id)
    REFERENCES widget.widgets(id)
    ON DELETE SET NULL;
