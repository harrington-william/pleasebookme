package com.pleasebookme.server.notification.preferences.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.notification.preferences.dto.NotificationPreferenceRequest;
import com.pleasebookme.server.notification.preferences.entity.NotificationPreferenceEntity;
import com.pleasebookme.server.notification.preferences.exception.DuplicateNotificationPreferenceException;
import com.pleasebookme.server.notification.preferences.exception.NotificationPreferenceNotFoundException;
import com.pleasebookme.server.notification.preferences.repository.NotificationPreferenceRepository;
import com.pleasebookme.server.notification.preferences.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final UserRepository userRepository;

    @Override
    public NotificationPreferenceEntity createNotificationPreference(NotificationPreferenceRequest request) {
        if (notificationPreferenceRepository.existsByUserUserIdAndNotificationType(request.userId(), request.notificationType())) {
            throw new DuplicateNotificationPreferenceException(
                "Notification preference already exists for user " + request.userId() + " and type " + request.notificationType()
            );
        }

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        NotificationPreferenceEntity.NotificationPreferenceEntityBuilder preference = NotificationPreferenceEntity.builder()
            .user(user)
            .notificationType(request.notificationType())
            .quietHoursStart(request.quietHoursStart())
            .quietHoursEnd(request.quietHoursEnd());

        if (request.emailEnabled() != null) preference.emailEnabled(request.emailEnabled());
        if (request.smsEnabled() != null) preference.smsEnabled(request.smsEnabled());
        if (request.pushEnabled() != null) preference.pushEnabled(request.pushEnabled());
        if (request.inAppEnabled() != null) preference.inAppEnabled(request.inAppEnabled());

        return notificationPreferenceRepository.save(preference.build());
    }

    @Override
    public NotificationPreferenceEntity getNotificationPreferenceById(BigInteger notificationPreferenceId) {
        return notificationPreferenceRepository.findById(notificationPreferenceId)
            .orElseThrow(() -> new NotificationPreferenceNotFoundException(
                "Notification preference not found: " + notificationPreferenceId
            ));
    }

    @Override
    public List<NotificationPreferenceEntity> getAllNotificationPreferences() {
        return notificationPreferenceRepository.findAll();
    }

    @Override
    public NotificationPreferenceEntity updateNotificationPreference(
        BigInteger notificationPreferenceId,
        NotificationPreferenceRequest request
    ) {
        NotificationPreferenceEntity preference = getNotificationPreferenceById(notificationPreferenceId);

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        preference.setUser(user);
        preference.setNotificationType(request.notificationType());
        preference.setQuietHoursStart(request.quietHoursStart());
        preference.setQuietHoursEnd(request.quietHoursEnd());

        if (request.emailEnabled() != null) preference.setEmailEnabled(request.emailEnabled());
        if (request.smsEnabled() != null) preference.setSmsEnabled(request.smsEnabled());
        if (request.pushEnabled() != null) preference.setPushEnabled(request.pushEnabled());
        if (request.inAppEnabled() != null) preference.setInAppEnabled(request.inAppEnabled());

        return notificationPreferenceRepository.save(preference);
    }

    @Override
    public void deleteNotificationPreference(BigInteger notificationPreferenceId) {
        notificationPreferenceRepository.delete(getNotificationPreferenceById(notificationPreferenceId));
    }
}
