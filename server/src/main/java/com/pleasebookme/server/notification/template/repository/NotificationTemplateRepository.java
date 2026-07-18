package com.pleasebookme.server.notification.template.repository;

import com.pleasebookme.server.notification.template.entity.NotificationTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, BigInteger> {
    boolean existsByTenantTenantIdAndCode(
        BigInteger tenantId,
        String code
    );
}
