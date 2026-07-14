CREATE TABLE audit.audit_changes (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_audit_change_resource
        FOREIGN KEY (resource_id)
        REFERENCES audit.audit_resources(id)
        ON DELETE CASCADE
);
