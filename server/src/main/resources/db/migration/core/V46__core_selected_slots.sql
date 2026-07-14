CREATE TABLE core.selected_slots (
    id BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL DEFAULT gen_random_uuid(),
    service_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    slot_start TIMESTAMPTZ NOT NULL,
    slot_end TIMESTAMPTZ NOT NULL,
    release_at TIMESTAMPTZ NOT NULL,
    is_seat BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_selected_slots_uid UNIQUE (uid),
    CONSTRAINT uq_selected_slots_slot
        UNIQUE (
            service_id,
            user_id,
            slot_start,
            slot_end
        ),

    CONSTRAINT fk_selected_slot_service
        FOREIGN KEY (service_id)
        REFERENCES core.services(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_selected_slot_user
        FOREIGN KEY (user_id)
        REFERENCES auth.users(id)
        ON DELETE CASCADE
);
