package com.pleasebookme.server.resource.resources.repository;

import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface ResourceRepository extends JpaRepository<ResourceEntity, BigInteger> {
    boolean existsByOrganizationOrganizationIdAndSlug(
        BigInteger organizationId,
        String slug
    );
}
