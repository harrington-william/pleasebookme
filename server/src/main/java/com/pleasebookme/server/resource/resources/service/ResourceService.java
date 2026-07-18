package com.pleasebookme.server.resource.resources.service;

import com.pleasebookme.server.resource.resources.dto.ResourceRequest;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;

import java.math.BigInteger;
import java.util.List;

public interface ResourceService {
    ResourceEntity createResource(ResourceRequest request);

    ResourceEntity getResourceById(BigInteger resourceId);

    List<ResourceEntity> getAllResources();

    ResourceEntity updateResource(
        BigInteger resourceId,
        ResourceRequest request
    );

    void deleteResource(BigInteger resourceId);
}
