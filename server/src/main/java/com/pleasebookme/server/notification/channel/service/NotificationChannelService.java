package com.pleasebookme.server.notification.channel.service;

import com.pleasebookme.server.notification.channel.dto.NotificationChannelRequest;
import com.pleasebookme.server.notification.channel.entity.NotificationChannelEntity;

import java.math.BigInteger;
import java.util.List;

public interface NotificationChannelService {
    NotificationChannelEntity createNotificationChannel(NotificationChannelRequest request);

    NotificationChannelEntity getNotificationChannelById(BigInteger notificationChannelId);

    List<NotificationChannelEntity> getAllNotificationChannels();

    NotificationChannelEntity updateNotificationChannel(
        BigInteger notificationChannelId,
        NotificationChannelRequest request
    );

    void deleteNotificationChannel(BigInteger notificationChannelId);
}
