package com.pleasebookme.server.notification.queue.dto;

import com.pleasebookme.server.notification.enums.NotificationStatus;
import com.pleasebookme.server.notification.queue.entity.NotificationQueueEntity;

import java.math.BigInteger;
import java.time.Instant;

public record NotificationQueueResponse(
    BigInteger notificationQueueId,
    BigInteger notificationId,
    NotificationStatus status,
    Instant availableAt,
    Integer attempts,
    Instant lastAttemptAt,
    Instant createdAt
) {
    public static NotificationQueueResponse from(NotificationQueueEntity queue) {
        return new NotificationQueueResponse(
            queue.getNotificationQueueId(),
            queue.getNotification().getNotificationId(),
            queue.getStatus(),
            queue.getAvailableAt(),
            queue.getAttempts(),
            queue.getLastAttemptAt(),
            queue.getCreatedAt()
        );
    }
}
