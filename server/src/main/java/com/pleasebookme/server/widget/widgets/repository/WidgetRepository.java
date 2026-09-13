package com.pleasebookme.server.widget.widgets.repository;

import com.pleasebookme.server.widget.enums.WidgetStatus;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WidgetRepository extends JpaRepository<WidgetEntity, BigInteger>, JpaSpecificationExecutor<WidgetEntity> {
    boolean existsByPublicKey(String publicKey);

    Optional<WidgetEntity> findByPublicKey(String publicKey);

    Optional<WidgetEntity> findByWidgetUid(UUID uid);

    long countByTenantTenantIdAndStatus(
        BigInteger tenantId,
        WidgetStatus status
    );

    long countByTenantTenantIdAndStatusNot(
        BigInteger tenantId,
        WidgetStatus status
    );
}
