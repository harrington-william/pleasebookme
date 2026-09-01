package com.pleasebookme.server.resource.resourceservice.repository;

import com.pleasebookme.server.resource.resourceservice.entity.ResourceServiceEntity;
import com.pleasebookme.server.resource.resourceservice.id.ResourceServiceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface ResourceServiceRepository extends JpaRepository<ResourceServiceEntity, ResourceServiceId> {
    List<ResourceServiceEntity> findByResourceResourceId(BigInteger resourceId);

    List<ResourceServiceEntity> findByServiceServiceId(BigInteger serviceId);

    List<ResourceServiceEntity> findByResourceOrganizationOrganizationId(BigInteger organizationId);
}
