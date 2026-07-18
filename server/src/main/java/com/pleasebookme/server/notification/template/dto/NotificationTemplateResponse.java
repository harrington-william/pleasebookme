package com.pleasebookme.server.notification.template.dto;

import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.notification.template.entity.NotificationTemplateEntity;

import java.math.BigInteger;
import java.time.Instant;

public record NotificationTemplateResponse(
    BigInteger notificationTemplateId,
    BigInteger tenantId,
    String code,
    String name,
    String channel,
    String subjectTemplate,
    String bodyTemplate,
    Locale locale,
    Boolean enabled,
    Integer version,
    Instant createdAt,
    Instant updatedAt
) {
    public static NotificationTemplateResponse from(NotificationTemplateEntity template) {
        return new NotificationTemplateResponse(
            template.getNotificationTemplateId(),
            template.getTenant() != null ? template.getTenant().getTenantId() : null,
            template.getCode(),
            template.getName(),
            template.getChannel(),
            template.getSubjectTemplate(),
            template.getBodyTemplate(),
            template.getLocale(),
            template.getEnabled(),
            template.getVersion(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );
    }
}
