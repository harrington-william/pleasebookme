package com.pleasebookme.server.resource.resourceservice.service;

import com.pleasebookme.server.resource.resourceservice.dto.ResourceServiceRequest;
import com.pleasebookme.server.resource.resourceservice.entity.ResourceServiceEntity;

import java.math.BigInteger;
import java.util.List;

public interface ResourceServiceService {
    ResourceServiceEntity createResourceService(ResourceServiceRequest request);

    ResourceServiceEntity getResourceServiceById(
        BigInteger resourceId,
        BigInteger serviceId
    );

    List<ResourceServiceEntity> getAllResourceServices();

    List<ResourceServiceEntity> getResourceServicesByResourceId(BigInteger resourceId);

    List<ResourceServiceEntity> getResourceServicesByOrganizationId(BigInteger organizationId);

    void deleteResourceService(
        BigInteger resourceId,
        BigInteger serviceId
    );
}
