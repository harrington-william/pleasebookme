package com.pleasebookme.server.notification.queue.dto;

import com.pleasebookme.server.notification.enums.NotificationStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.time.Instant;

public record NotificationQueueRequest(
    @NotNull
    BigInteger notificationId,

    NotificationStatus status,

    @NotNull
    Instant availableAt,

    Integer attempts,

    Instant lastAttemptAt
) {
}
