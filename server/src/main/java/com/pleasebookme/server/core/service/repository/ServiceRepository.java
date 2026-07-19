package com.pleasebookme.server.core.service.repository;

import com.pleasebookme.server.core.service.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, BigInteger> {
    boolean existsByOrganizationOrganizationIdAndSlug(
        BigInteger organizationId,
        String slug
    );
}
