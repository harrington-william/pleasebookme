CREATE TYPE resource.resource_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'MAINTENANCE',
    'RETIRED'
);

ALTER TABLE resource.resources
ALTER COLUMN status TYPE resource.resource_status
USING upper(trim(status))::resource.resource_status;
