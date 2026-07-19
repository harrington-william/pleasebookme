package com.pleasebookme.server.notification.delivery.dto;

import com.pleasebookme.server.notification.delivery.entity.NotificationDeliveryEntity;
import com.pleasebookme.server.notification.enums.NotificationStatus;

import java.math.BigInteger;
import java.time.Instant;

public record NotificationDeliveryResponse(
    BigInteger notificationDeliveryId,
    BigInteger notificationId,
    String provider,
    String providerMessageId,
    NotificationStatus status,
    Integer attempt,
    String errorMessage,
    Instant sentAt,
    Instant createdAt
) {
    public static NotificationDeliveryResponse from(NotificationDeliveryEntity delivery) {
        return new NotificationDeliveryResponse(
            delivery.getNotificationDeliveryId(),
            delivery.getNotification().getNotificationId(),
            delivery.getProvider(),
            delivery.getProviderMessageId(),
            delivery.getStatus(),
            delivery.getAttempt(),
            delivery.getErrorMessage(),
            delivery.getSentAt(),
            delivery.getCreatedAt()
        );
    }
}
