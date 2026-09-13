-- Icons are a presentation aid and are not required to classify a resource.
ALTER TABLE resource.resource_types ALTER COLUMN icon DROP NOT NULL;
