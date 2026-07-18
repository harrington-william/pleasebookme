package com.pleasebookme.server.notification.channel.dto;

import com.pleasebookme.server.notification.channel.entity.NotificationChannelEntity;

import java.math.BigInteger;
import java.time.Instant;

public record NotificationChannelResponse(
    BigInteger notificationChannelId,
    String code,
    String name,
    Boolean enabled,
    Instant createdAt
) {
    public static NotificationChannelResponse from(NotificationChannelEntity channel) {
        return new NotificationChannelResponse(
            channel.getNotificationChannelId(),
            channel.getCode(),
            channel.getName(),
            channel.getEnabled(),
            channel.getCreatedAt()
        );
    }
}
