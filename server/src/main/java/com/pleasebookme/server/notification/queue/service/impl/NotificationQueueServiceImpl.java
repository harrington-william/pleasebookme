package com.pleasebookme.server.notification.queue.service.impl;

import com.pleasebookme.server.notification.notifications.entity.NotificationEntity;
import com.pleasebookme.server.notification.notifications.exception.NotificationNotFoundException;
import com.pleasebookme.server.notification.notifications.repository.NotificationRepository;
import com.pleasebookme.server.notification.queue.dto.NotificationQueueRequest;
import com.pleasebookme.server.notification.queue.entity.NotificationQueueEntity;
import com.pleasebookme.server.notification.queue.exception.NotificationQueueNotFoundException;
import com.pleasebookme.server.notification.queue.repository.NotificationQueueRepository;
import com.pleasebookme.server.notification.queue.service.NotificationQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationQueueServiceImpl implements NotificationQueueService {
    private final NotificationQueueRepository notificationQueueRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public NotificationQueueEntity createNotificationQueue(NotificationQueueRequest request) {
        NotificationEntity notification = notificationRepository.findById(request.notificationId())
            .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + request.notificationId()));

        NotificationQueueEntity.NotificationQueueEntityBuilder queue = NotificationQueueEntity.builder()
            .notification(notification)
            .availableAt(request.availableAt())
            .lastAttemptAt(request.lastAttemptAt());

        if (request.status() != null) queue.status(request.status());
        if (request.attempts() != null) queue.attempts(request.attempts());

        return notificationQueueRepository.save(queue.build());
    }

    @Override
    public NotificationQueueEntity getNotificationQueueById(BigInteger notificationQueueId) {
        return notificationQueueRepository.findById(notificationQueueId)
            .orElseThrow(() -> new NotificationQueueNotFoundException(
                "Notification queue not found: " + notificationQueueId
            ));
    }

    @Override
    public List<NotificationQueueEntity> getAllNotificationQueues() {
        return notificationQueueRepository.findAll();
    }

    @Override
    public NotificationQueueEntity updateNotificationQueue(
        BigInteger notificationQueueId,
        NotificationQueueRequest request
    ) {
        NotificationQueueEntity queue = getNotificationQueueById(notificationQueueId);

        NotificationEntity notification = notificationRepository.findById(request.notificationId())
            .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + request.notificationId()));

        queue.setNotification(notification);
        queue.setAvailableAt(request.availableAt());
        queue.setLastAttemptAt(request.lastAttemptAt());

        if (request.status() != null) queue.setStatus(request.status());
        if (request.attempts() != null) queue.setAttempts(request.attempts());

        return notificationQueueRepository.save(queue);
    }

    @Override
    public void deleteNotificationQueue(BigInteger notificationQueueId) {
        notificationQueueRepository.delete(getNotificationQueueById(notificationQueueId));
    }
}
