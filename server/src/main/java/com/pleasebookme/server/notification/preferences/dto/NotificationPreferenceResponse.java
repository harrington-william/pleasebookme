package com.pleasebookme.server.notification.preferences.dto;

import com.pleasebookme.server.notification.preferences.entity.NotificationPreferenceEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalTime;

public record NotificationPreferenceResponse(
    BigInteger notificationPreferenceId,
    BigInteger userId,
    String notificationType,
    Boolean emailEnabled,
    Boolean smsEnabled,
    Boolean pushEnabled,
    Boolean inAppEnabled,
    LocalTime quietHoursStart,
    LocalTime quietHoursEnd,
    Instant updatedAt
) {
    public static NotificationPreferenceResponse from(NotificationPreferenceEntity preference) {
        return new NotificationPreferenceResponse(
            preference.getNotificationPreferenceId(),
            preference.getUser().getUserId(),
            preference.getNotificationType(),
            preference.getEmailEnabled(),
            preference.getSmsEnabled(),
            preference.getPushEnabled(),
            preference.getInAppEnabled(),
            preference.getQuietHoursStart(),
            preference.getQuietHoursEnd(),
            preference.getUpdatedAt()
        );
    }
}
