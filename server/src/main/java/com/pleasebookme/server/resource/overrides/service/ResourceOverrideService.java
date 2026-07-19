package com.pleasebookme.server.resource.overrides.service;

import com.pleasebookme.server.resource.overrides.dto.ResourceOverrideRequest;
import com.pleasebookme.server.resource.overrides.entity.ResourceOverrideEntity;

import java.math.BigInteger;
import java.util.List;

public interface ResourceOverrideService {
    ResourceOverrideEntity createResourceOverride(ResourceOverrideRequest request);

    ResourceOverrideEntity getResourceOverrideById(BigInteger resourceOverrideId);

    List<ResourceOverrideEntity> getAllResourceOverrides();

    ResourceOverrideEntity updateResourceOverride(
        BigInteger resourceOverrideId,
        ResourceOverrideRequest request
    );

    void deleteResourceOverride(BigInteger resourceOverrideId);
}
