package com.pleasebookme.server.resource.overrides.service.impl;

import com.pleasebookme.server.resource.overrides.dto.ResourceOverrideRequest;
import com.pleasebookme.server.resource.overrides.entity.ResourceOverrideEntity;
import com.pleasebookme.server.resource.overrides.exception.ResourceOverrideNotFoundException;
import com.pleasebookme.server.resource.overrides.repository.ResourceOverrideRepository;
import com.pleasebookme.server.resource.overrides.service.ResourceOverrideService;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import com.pleasebookme.server.resource.resources.exception.ResourceNotFoundException;
import com.pleasebookme.server.resource.resources.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceOverrideServiceImpl implements ResourceOverrideService {
    private final ResourceOverrideRepository resourceOverrideRepository;
    private final ResourceRepository resourceRepository;

    @Override
    public ResourceOverrideEntity createResourceOverride(ResourceOverrideRequest request) {
        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        ResourceOverrideEntity resourceOverride = ResourceOverrideEntity.builder()
            .resource(resource)
            .startTime(request.startTime())
            .endTime(request.endTime())
            .reason(request.reason())
            .build();

        return resourceOverrideRepository.save(resourceOverride);
    }

    @Override
    public ResourceOverrideEntity getResourceOverrideById(BigInteger resourceOverrideId) {
        return resourceOverrideRepository.findById(resourceOverrideId)
            .orElseThrow(() -> new ResourceOverrideNotFoundException("Resource override not found: " + resourceOverrideId));
    }

    @Override
    public List<ResourceOverrideEntity> getAllResourceOverrides() {
        return resourceOverrideRepository.findAll();
    }

    @Override
    public ResourceOverrideEntity updateResourceOverride(
        BigInteger resourceOverrideId,
        ResourceOverrideRequest request
    ) {
        ResourceOverrideEntity resourceOverride = getResourceOverrideById(resourceOverrideId);

        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        resourceOverride.setResource(resource);
        resourceOverride.setStartTime(request.startTime());
        resourceOverride.setEndTime(request.endTime());
        resourceOverride.setReason(request.reason());

        return resourceOverrideRepository.save(resourceOverride);
    }

    @Override
    public void deleteResourceOverride(BigInteger resourceOverrideId) {
        resourceOverrideRepository.delete(getResourceOverrideById(resourceOverrideId));
    }
}
