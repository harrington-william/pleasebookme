package com.pleasebookme.server.resource.type.repository;

import com.pleasebookme.server.resource.type.entity.ResourceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface ResourceTypeRepository extends JpaRepository<ResourceTypeEntity, BigInteger> {
    List<ResourceTypeEntity> findByOrganizationOrganizationId(BigInteger organizationId);
}
