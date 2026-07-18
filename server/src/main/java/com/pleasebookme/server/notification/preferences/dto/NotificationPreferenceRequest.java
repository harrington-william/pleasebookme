package com.pleasebookme.server.notification.preferences.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.time.LocalTime;

public record NotificationPreferenceRequest(
    @NotNull
    BigInteger userId,

    @NotBlank
    @Size(max = 50)
    String notificationType,

    Boolean emailEnabled,

    Boolean smsEnabled,

    Boolean pushEnabled,

    Boolean inAppEnabled,

    LocalTime quietHoursStart,

    LocalTime quietHoursEnd
) {
}
