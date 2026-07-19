CREATE TABLE notification.notification_deliveries (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    provider VARCHAR(100) NOT NULL,
    provider_message_id VARCHAR(255),
    status notification.notification_status NOT NULL,
    attempt INTEGER NOT NULL,
    error_message TEXT,
    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_notification_delivery_notification
        FOREIGN KEY (notification_id)
        REFERENCES notification.notifications(id)
        ON DELETE CASCADE
);
