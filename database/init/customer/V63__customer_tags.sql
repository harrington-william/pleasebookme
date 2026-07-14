-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 7 - Customer - Customer Tags
-- =====================================================

-- `tag` kept free-text (VARCHAR), not a hard enum - CUSTOMER_SCHEMA.md
-- lists its values under "### Examples" (illustrative), not "(Enum)"
-- like the platform's genuinely fixed enums.
CREATE TABLE customer.customer_tags (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_customer_tags_customer_tag UNIQUE (customer_id, tag),

    CONSTRAINT fk_customer_tag_customer FOREIGN KEY (customer_id) REFERENCES customer.customers(id) ON DELETE CASCADE
);
