CREATE TABLE resource.resource_calendars (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_resource_calendar_resource
        FOREIGN KEY (resource_id)
        REFERENCES resource.resources(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_resource_calendar_schedule
        FOREIGN KEY (schedule_id)
        REFERENCES core.schedules(id)
        ON DELETE CASCADE
);
