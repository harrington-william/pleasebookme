package com.pleasebookme.server.notification.preferences.service;

import com.pleasebookme.server.notification.preferences.dto.NotificationPreferenceRequest;
import com.pleasebookme.server.notification.preferences.entity.NotificationPreferenceEntity;

import java.math.BigInteger;
import java.util.List;

public interface NotificationPreferenceService {
    NotificationPreferenceEntity createNotificationPreference(NotificationPreferenceRequest request);

    NotificationPreferenceEntity getNotificationPreferenceById(BigInteger notificationPreferenceId);

    List<NotificationPreferenceEntity> getAllNotificationPreferences();

    NotificationPreferenceEntity updateNotificationPreference(
        BigInteger notificationPreferenceId,
        NotificationPreferenceRequest request
    );

    void deleteNotificationPreference(BigInteger notificationPreferenceId);
}
