package com.pleasebookme.server.resource.resources.repository;

import com.pleasebookme.server.resource.enums.ResourceStatus;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface ResourceRepository
    extends JpaRepository<ResourceEntity, BigInteger>, JpaSpecificationExecutor<ResourceEntity> {

    boolean existsByOrganizationOrganizationIdAndSlug(
        BigInteger organizationId,
        String slug
    );

    Page<ResourceEntity> findByOrganizationOrganizationId(
        BigInteger organizationId,
        Pageable pageable
    );

    List<ResourceEntity> findByOrganizationOrganizationId(BigInteger organizationId);

    long countByOrganizationOrganizationId(BigInteger organizationId);

    long countByOrganizationOrganizationIdAndStatus(
        BigInteger organizationId,
        ResourceStatus status
    );
}
