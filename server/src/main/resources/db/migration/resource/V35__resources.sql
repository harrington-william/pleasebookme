CREATE TABLE resource.resources (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    organization_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    resource_type_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    capacity INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    is_bookable BOOLEAN NOT NULL DEFAULT true,
    is_virtual BOOLEAN NOT NULL DEFAULT false,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_resources_uid UNIQUE (uid),
    CONSTRAINT uq_resources_organization_slug UNIQUE (organization_id, slug),

    CONSTRAINT fk_resource_organization
        FOREIGN KEY (organization_id)
        REFERENCES organization.organizations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_resource_service
        FOREIGN KEY (service_id)
        REFERENCES core.services(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_resource_type
        FOREIGN KEY (resource_type_id)
        REFERENCES resource.resource_types(id)
        ON DELETE CASCADE
);
