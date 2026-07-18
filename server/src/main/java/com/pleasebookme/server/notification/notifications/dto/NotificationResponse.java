package com.pleasebookme.server.notification.notifications.dto;

import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.notification.enums.NotificationPriority;
import com.pleasebookme.server.notification.enums.NotificationStatus;
import com.pleasebookme.server.notification.enums.RecipientType;
import com.pleasebookme.server.notification.notifications.entity.NotificationEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    BigInteger notificationId,
    UUID notificationUid,
    BigInteger tenantId,
    BigInteger organizationId,
    RecipientType recipientType,
    String recipientUid,
    BigInteger templateId,
    BigInteger channelId,
    NotificationStatus status,
    NotificationPriority priority,
    String subject,
    String content,
    Locale locale,
    Instant scheduledAt,
    Instant sentAt,
    Instant createdAt,
    Instant updatedAt
) {
    public static NotificationResponse from(NotificationEntity notification) {
        return new NotificationResponse(
            notification.getNotificationId(),
            notification.getNotificationUid(),
            notification.getTenant().getTenantId(),
            notification.getOrganization().getOrganizationId(),
            notification.getRecipientType(),
            notification.getRecipientUid(),
            notification.getTemplate().getNotificationTemplateId(),
            notification.getChannel().getNotificationChannelId(),
            notification.getStatus(),
            notification.getPriority(),
            notification.getSubject(),
            notification.getContent(),
            notification.getLocale(),
            notification.getScheduledAt(),
            notification.getSentAt(),
            notification.getCreatedAt(),
            notification.getUpdatedAt()
        );
    }
}
