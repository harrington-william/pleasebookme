-- =====================================================
-- PleaseBookMe Platform
-- Database Initialization
-- Phase 12 - Indexes - Customer
-- =====================================================

-- tenant_id already covered as the leftmost column of
-- uq_customers_tenant_email / uq_customers_tenant_phone.

CREATE INDEX idx_customers_organization_id ON customer.customers(organization_id);
CREATE INDEX idx_customers_email ON customer.customers(email);
CREATE INDEX idx_customers_phone ON customer.customers(phone);
CREATE INDEX idx_customers_name ON customer.customers(name);
