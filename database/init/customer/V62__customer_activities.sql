-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 7 - Customer - Customer Activities
-- =====================================================

-- `activity_type` and `reference_type` kept as VARCHAR - polymorphic,
-- no fixed value set documented. `reference_uid` is a loose polymorphic
-- reference (no FK), matching the pattern used by audit.audit_resources.
CREATE TABLE customer.customer_activities (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    reference_uid VARCHAR(255) NOT NULL,
    description TEXT,
    metadata JSONB,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_customer_activity_customer
        FOREIGN KEY (customer_id)
        REFERENCES customer.customers(id)
        ON DELETE CASCADE
);
