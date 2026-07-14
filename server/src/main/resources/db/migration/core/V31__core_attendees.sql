CREATE TABLE core.attendees (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    locale public.locale,
    timezone VARCHAR(100),
    no_show BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_attendee_booking FOREIGN KEY (booking_id) REFERENCES core.bookings(id) ON DELETE CASCADE
);
