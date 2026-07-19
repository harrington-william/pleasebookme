package com.pleasebookme.server.notification.channel.repository;

import com.pleasebookme.server.notification.channel.entity.NotificationChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannelEntity, BigInteger> {
    boolean existsByCode(String code);
}
