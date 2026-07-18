package com.pleasebookme.server.notification.delivery.repository;

import com.pleasebookme.server.notification.delivery.entity.NotificationDeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDeliveryEntity, BigInteger> {
}
