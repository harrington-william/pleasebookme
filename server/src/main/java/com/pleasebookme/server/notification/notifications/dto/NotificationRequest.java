package com.pleasebookme.server.notification.notifications.dto;

import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.notification.enums.NotificationPriority;
import com.pleasebookme.server.notification.enums.NotificationStatus;
import com.pleasebookme.server.notification.enums.RecipientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.time.Instant;

public record NotificationRequest(
    @NotNull
    BigInteger tenantId,

    @NotNull
    BigInteger organizationId,

    @NotNull
    RecipientType recipientType,

    @NotBlank
    @Size(max = 255)
    String recipientUid,

    @NotNull
    BigInteger templateId,

    @NotNull
    BigInteger channelId,

    NotificationStatus status,

    NotificationPriority priority,

    String subject,

    String content,

    @NotNull
    Locale locale,

    @NotNull
    Instant scheduledAt,

    Instant sentAt
) {
}
