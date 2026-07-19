package com.pleasebookme.server.notification.delivery.service.impl;

import com.pleasebookme.server.notification.delivery.dto.NotificationDeliveryRequest;
import com.pleasebookme.server.notification.delivery.entity.NotificationDeliveryEntity;
import com.pleasebookme.server.notification.delivery.exception.NotificationDeliveryNotFoundException;
import com.pleasebookme.server.notification.delivery.repository.NotificationDeliveryRepository;
import com.pleasebookme.server.notification.delivery.service.NotificationDeliveryService;
import com.pleasebookme.server.notification.notifications.entity.NotificationEntity;
import com.pleasebookme.server.notification.notifications.exception.NotificationNotFoundException;
import com.pleasebookme.server.notification.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationDeliveryServiceImpl implements NotificationDeliveryService {
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public NotificationDeliveryEntity createNotificationDelivery(NotificationDeliveryRequest request) {
        NotificationEntity notification = notificationRepository.findById(request.notificationId())
            .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + request.notificationId()));

        NotificationDeliveryEntity delivery = NotificationDeliveryEntity.builder()
            .notification(notification)
            .provider(request.provider())
            .providerMessageId(request.providerMessageId())
            .status(request.status())
            .attempt(request.attempt())
            .errorMessage(request.errorMessage())
            .sentAt(request.sentAt())
            .build();

        return notificationDeliveryRepository.save(delivery);
    }

    @Override
    public NotificationDeliveryEntity getNotificationDeliveryById(BigInteger notificationDeliveryId) {
        return notificationDeliveryRepository.findById(notificationDeliveryId)
            .orElseThrow(() -> new NotificationDeliveryNotFoundException(
                "Notification delivery not found: " + notificationDeliveryId
            ));
    }

    @Override
    public List<NotificationDeliveryEntity> getAllNotificationDeliveries() {
        return notificationDeliveryRepository.findAll();
    }

    @Override
    public NotificationDeliveryEntity updateNotificationDelivery(
        BigInteger notificationDeliveryId,
        NotificationDeliveryRequest request
    ) {
        NotificationDeliveryEntity delivery = getNotificationDeliveryById(notificationDeliveryId);

        NotificationEntity notification = notificationRepository.findById(request.notificationId())
            .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + request.notificationId()));

        delivery.setNotification(notification);
        delivery.setProvider(request.provider());
        delivery.setProviderMessageId(request.providerMessageId());
        delivery.setStatus(request.status());
        delivery.setAttempt(request.attempt());
        delivery.setErrorMessage(request.errorMessage());
        delivery.setSentAt(request.sentAt());

        return notificationDeliveryRepository.save(delivery);
    }

    @Override
    public void deleteNotificationDelivery(BigInteger notificationDeliveryId) {
        notificationDeliveryRepository.delete(getNotificationDeliveryById(notificationDeliveryId));
    }
}
