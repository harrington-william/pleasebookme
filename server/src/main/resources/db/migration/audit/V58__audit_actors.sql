CREATE TABLE audit.audit_actors (
    id BIGSERIAL PRIMARY KEY,
    actor_type audit.audit_actor_type NOT NULL,
    user_uid VARCHAR(255),
    membership_id BIGINT,
    widget_uid VARCHAR(255),
    api_key_uid VARCHAR(255),
    attendee_id BIGINT,
    system_name VARCHAR(100),
    display_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    ip_address INET NOT NULL,
    user_agent TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_audit_actors_user_uid UNIQUE (user_uid),
    CONSTRAINT uq_audit_actors_attendee_id UNIQUE (attendee_id),
    CONSTRAINT uq_audit_actors_email UNIQUE (email)
);
