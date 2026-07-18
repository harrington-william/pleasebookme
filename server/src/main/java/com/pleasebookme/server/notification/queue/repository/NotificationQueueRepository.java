package com.pleasebookme.server.notification.queue.repository;

import com.pleasebookme.server.notification.queue.entity.NotificationQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface NotificationQueueRepository extends JpaRepository<NotificationQueueEntity, BigInteger> {
}
