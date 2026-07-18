package com.pleasebookme.server.notification.delivery.dto;

import com.pleasebookme.server.notification.enums.NotificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.time.Instant;

public record NotificationDeliveryRequest(
    @NotNull
    BigInteger notificationId,

    @NotBlank
    @Size(max = 100)
    String provider,

    @Size(max = 255)
    String providerMessageId,

    @NotNull
    NotificationStatus status,

    @NotNull
    Integer attempt,

    String errorMessage,

    Instant sentAt
) {
}
