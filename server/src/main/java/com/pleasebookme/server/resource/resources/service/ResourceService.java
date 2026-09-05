package com.pleasebookme.server.resource.resources.service;

import com.pleasebookme.server.resource.resources.dto.ResourceFilter;
import com.pleasebookme.server.resource.resources.dto.ResourceRequest;
import com.pleasebookme.server.resource.resources.dto.ResourceStatsResponse;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;
import java.util.List;

public interface ResourceService {
    ResourceEntity createResource(ResourceRequest request);

    ResourceEntity getResourceById(BigInteger resourceId);

    Page<ResourceEntity> getResourcesByOrganizationId(
        BigInteger organizationId,
        ResourceFilter filter,
        Pageable pageable
    );

    List<ResourceEntity> getResourceLookupByOrganizationId(BigInteger organizationId);

    ResourceStatsResponse getResourceStatsByOrganizationId(BigInteger organizationId);

    ResourceEntity updateResource(
        BigInteger resourceId,
        ResourceRequest request
    );

    void deleteResource(BigInteger resourceId);
}
