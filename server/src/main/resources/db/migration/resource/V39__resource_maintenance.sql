CREATE TABLE resource.resource_maintenance (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_resource_maintenance_resource
        FOREIGN KEY (resource_id)
        REFERENCES resource.resources(id)
        ON DELETE CASCADE
);
