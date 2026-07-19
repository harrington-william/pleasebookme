package com.pleasebookme.server.notification.notifications.service;

import com.pleasebookme.server.notification.notifications.dto.NotificationRequest;
import com.pleasebookme.server.notification.notifications.entity.NotificationEntity;

import java.math.BigInteger;
import java.util.List;

public interface NotificationService {
    NotificationEntity createNotification(NotificationRequest request);

    NotificationEntity getNotificationById(BigInteger notificationId);

    List<NotificationEntity> getAllNotifications();

    NotificationEntity updateNotification(
        BigInteger notificationId,
        NotificationRequest request
    );

    void deleteNotification(BigInteger notificationId);
}
