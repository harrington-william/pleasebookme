package com.pleasebookme.server.core.service.repository;

import com.pleasebookme.server.core.service.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, BigInteger> {
    boolean existsByOrganizationOrganizationIdAndSlug(
        BigInteger organizationId,
        String slug
    );

    List<ServiceEntity> findByOrganizationOrganizationId(BigInteger organizationId);

    Optional<ServiceEntity> findByOrganizationOrganizationIdAndSlug(
        BigInteger organizationId,
        String slug
    );
}
