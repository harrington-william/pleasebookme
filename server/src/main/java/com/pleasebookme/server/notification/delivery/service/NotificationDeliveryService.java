package com.pleasebookme.server.notification.delivery.service;

import com.pleasebookme.server.notification.delivery.dto.NotificationDeliveryRequest;
import com.pleasebookme.server.notification.delivery.entity.NotificationDeliveryEntity;

import java.math.BigInteger;
import java.util.List;

public interface NotificationDeliveryService {
    NotificationDeliveryEntity createNotificationDelivery(NotificationDeliveryRequest request);

    NotificationDeliveryEntity getNotificationDeliveryById(BigInteger notificationDeliveryId);

    List<NotificationDeliveryEntity> getAllNotificationDeliveries();

    NotificationDeliveryEntity updateNotificationDelivery(
        BigInteger notificationDeliveryId,
        NotificationDeliveryRequest request
    );

    void deleteNotificationDelivery(BigInteger notificationDeliveryId);
}
