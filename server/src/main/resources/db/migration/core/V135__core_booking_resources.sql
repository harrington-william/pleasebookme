CREATE TABLE core.booking_resources (
    booking_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT false,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (booking_id, resource_id),

    CONSTRAINT fk_booking_resource_booking FOREIGN KEY (booking_id) REFERENCES core.bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_resource_resource FOREIGN KEY (resource_id) REFERENCES resource.resources(id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX uq_booking_resources_primary ON core.booking_resources(booking_id) WHERE is_primary;

CREATE INDEX idx_booking_resources_resource_id ON core.booking_resources(resource_id);
