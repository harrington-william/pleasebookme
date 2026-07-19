CREATE TABLE resource.resource_assignments (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    membership_id BIGINT NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    released_at TIMESTAMPTZ,
    assigned_by BIGINT,
    released_by BIGINT,
    is_primary BOOLEAN NOT NULL DEFAULT true,

    CONSTRAINT fk_resource_assignment_resource
        FOREIGN KEY (resource_id)
        REFERENCES resource.resources(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_resource_assignment_membership
        FOREIGN KEY (membership_id)
        REFERENCES organization.memberships(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_resource_assignment_assigned_by
        FOREIGN KEY (assigned_by)
        REFERENCES auth.users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_resource_assignment_released_by
        FOREIGN KEY (released_by)
        REFERENCES auth.users(id)
        ON DELETE SET NULL
);
