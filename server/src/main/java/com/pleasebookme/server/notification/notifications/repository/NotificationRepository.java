package com.pleasebookme.server.notification.notifications.repository;

import com.pleasebookme.server.notification.notifications.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, BigInteger> {
}
