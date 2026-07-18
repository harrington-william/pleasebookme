package com.pleasebookme.server.notification.preferences.repository;

import com.pleasebookme.server.notification.preferences.entity.NotificationPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreferenceEntity, BigInteger> {
    boolean existsByUserUserIdAndNotificationType(
        BigInteger userId,
        String notificationType
    );
}
