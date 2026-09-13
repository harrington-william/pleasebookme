package com.pleasebookme.server.resource.type.service;

import com.pleasebookme.server.resource.type.dto.ResourceTypeRequest;
import com.pleasebookme.server.resource.type.entity.ResourceTypeEntity;

import java.math.BigInteger;
import java.util.List;

public interface ResourceTypeService {
    ResourceTypeEntity createResourceType(ResourceTypeRequest request);

    ResourceTypeEntity getResourceTypeById(BigInteger resourceTypeId);

    List<ResourceTypeEntity> getResourceTypesByOrganizationId(BigInteger organizationId);

    ResourceTypeEntity updateResourceType(
        BigInteger resourceTypeId,
        ResourceTypeRequest request
    );

    void deleteResourceType(BigInteger resourceTypeId);
}
