package com.pleasebookme.server.notification.queue.service;

import com.pleasebookme.server.notification.queue.dto.NotificationQueueRequest;
import com.pleasebookme.server.notification.queue.entity.NotificationQueueEntity;

import java.math.BigInteger;
import java.util.List;

public interface NotificationQueueService {
    NotificationQueueEntity createNotificationQueue(NotificationQueueRequest request);

    NotificationQueueEntity getNotificationQueueById(BigInteger notificationQueueId);

    List<NotificationQueueEntity> getAllNotificationQueues();

    NotificationQueueEntity updateNotificationQueue(
        BigInteger notificationQueueId,
        NotificationQueueRequest request
    );

    void deleteNotificationQueue(BigInteger notificationQueueId);
}
