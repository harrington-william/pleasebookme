-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 7 - Customer - Customer Sources
-- =====================================================

-- `source` kept free-text (VARCHAR), same reasoning as customer_tags.tag -
-- listed under "### Examples" in CUSTOMER_SCHEMA.md, not "(Enum)".
CREATE TABLE customer.customer_sources (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    source VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_customer_source_customer
        FOREIGN KEY (customer_id)
        REFERENCES customer.customers(id)
        ON DELETE CASCADE
);
