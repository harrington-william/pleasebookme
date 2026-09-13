-- =====================================================
-- PleaseBookMe Platform
-- Resource-to-service assignments
-- =====================================================

-- A resource may serve multiple bookable services. The pair is the identity;
-- reassignment is represented by deleting or inserting the pair.
CREATE TABLE resource.resource_services (
    resource_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (resource_id, service_id),

    CONSTRAINT fk_resource_service_resource
        FOREIGN KEY (resource_id)
        REFERENCES resource.resources(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_resource_service_service
        FOREIGN KEY (service_id)
        REFERENCES core.services(id)
        ON DELETE CASCADE
);
