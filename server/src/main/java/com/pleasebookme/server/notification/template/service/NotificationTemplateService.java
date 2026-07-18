package com.pleasebookme.server.notification.template.service;

import com.pleasebookme.server.notification.template.dto.NotificationTemplateRequest;
import com.pleasebookme.server.notification.template.entity.NotificationTemplateEntity;

import java.math.BigInteger;
import java.util.List;

public interface NotificationTemplateService {
    NotificationTemplateEntity createNotificationTemplate(NotificationTemplateRequest request);

    NotificationTemplateEntity getNotificationTemplateById(BigInteger notificationTemplateId);

    List<NotificationTemplateEntity> getAllNotificationTemplates();

    NotificationTemplateEntity updateNotificationTemplate(
        BigInteger notificationTemplateId,
        NotificationTemplateRequest request
    );

    void deleteNotificationTemplate(BigInteger notificationTemplateId);
}
