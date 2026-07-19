ALTER TABLE resource.resource_attributes ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now();

ALTER TABLE resource.resource_attributes ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();