CREATE TABLE notification.notification_queue (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    status notification.notification_status NOT NULL DEFAULT 'QUEUED',
    available_at TIMESTAMPTZ NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_notification_queue_notification
        FOREIGN KEY (notification_id)
        REFERENCES notification.notifications(id)
        ON DELETE CASCADE
);
