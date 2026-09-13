ALTER TABLE resource.resource_types
ADD CONSTRAINT uq_resource_types_organization_name UNIQUE (organization_id, name);
