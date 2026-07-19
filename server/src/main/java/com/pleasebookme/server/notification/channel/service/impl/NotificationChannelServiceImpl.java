package com.pleasebookme.server.notification.channel.service.impl;

import com.pleasebookme.server.notification.channel.dto.NotificationChannelRequest;
import com.pleasebookme.server.notification.channel.entity.NotificationChannelEntity;
import com.pleasebookme.server.notification.channel.exception.DuplicateNotificationChannelException;
import com.pleasebookme.server.notification.channel.exception.NotificationChannelNotFoundException;
import com.pleasebookme.server.notification.channel.repository.NotificationChannelRepository;
import com.pleasebookme.server.notification.channel.service.NotificationChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationChannelServiceImpl implements NotificationChannelService {
    private final NotificationChannelRepository notificationChannelRepository;

    @Override
    public NotificationChannelEntity createNotificationChannel(NotificationChannelRequest request) {
        if (notificationChannelRepository.existsByCode(request.code())) {
            throw new DuplicateNotificationChannelException("Code already exists: " + request.code());
        }

        NotificationChannelEntity.NotificationChannelEntityBuilder channel = NotificationChannelEntity.builder()
            .code(request.code())
            .name(request.name());

        if (request.enabled() != null) channel.enabled(request.enabled());

        return notificationChannelRepository.save(channel.build());
    }

    @Override
    public NotificationChannelEntity getNotificationChannelById(BigInteger notificationChannelId) {
        return notificationChannelRepository.findById(notificationChannelId)
            .orElseThrow(() -> new NotificationChannelNotFoundException(
                "Notification channel not found: " + notificationChannelId
            ));
    }

    @Override
    public List<NotificationChannelEntity> getAllNotificationChannels() {
        return notificationChannelRepository.findAll();
    }

    @Override
    public NotificationChannelEntity updateNotificationChannel(
        BigInteger notificationChannelId,
        NotificationChannelRequest request
    ) {
        NotificationChannelEntity channel = getNotificationChannelById(notificationChannelId);

        channel.setCode(request.code());
        channel.setName(request.name());

        if (request.enabled() != null) channel.setEnabled(request.enabled());

        return notificationChannelRepository.save(channel);
    }

    @Override
    public void deleteNotificationChannel(BigInteger notificationChannelId) {
        notificationChannelRepository.delete(getNotificationChannelById(notificationChannelId));
    }
}
