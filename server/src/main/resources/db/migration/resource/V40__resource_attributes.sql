CREATE TABLE resource.resource_attributes (
    resource_id BIGINT NOT NULL,
    key VARCHAR(100) NOT NULL,
    value TEXT NOT NULL,

    PRIMARY KEY (resource_id, key),

    CONSTRAINT fk_resource_attribute_resource
        FOREIGN KEY (resource_id)
        REFERENCES resource.resources(id)
        ON DELETE CASCADE
);
