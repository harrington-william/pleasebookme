package com.pleasebookme.server.resource.resourceservice.service.impl;

import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import com.pleasebookme.server.resource.resources.exception.ResourceNotFoundException;
import com.pleasebookme.server.resource.resources.repository.ResourceRepository;
import com.pleasebookme.server.resource.resourceservice.dto.ResourceServiceRequest;
import com.pleasebookme.server.resource.resourceservice.entity.ResourceServiceEntity;
import com.pleasebookme.server.resource.resourceservice.exception.DuplicateResourceServiceException;
import com.pleasebookme.server.resource.resourceservice.exception.ResourceServiceNotFoundException;
import com.pleasebookme.server.resource.resourceservice.id.ResourceServiceId;
import com.pleasebookme.server.resource.resourceservice.repository.ResourceServiceRepository;
import com.pleasebookme.server.resource.resourceservice.service.ResourceServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceServiceImpl implements ResourceServiceService {
    private final ResourceServiceRepository resourceServiceRepository;
    private final ResourceRepository resourceRepository;
    private final ServiceRepository serviceRepository;

    @Override
    public ResourceServiceEntity createResourceService(ResourceServiceRequest request) {
        ResourceServiceId resourceServiceId = new ResourceServiceId(
            request.resourceId(),
            request.serviceId()
        );

        // A non-null composite id makes JpaRepository.save use merge, so duplicates must be rejected first.
        if (resourceServiceRepository.existsById(resourceServiceId)) {
            throw new DuplicateResourceServiceException(
                "Service " + request.serviceId() + " is already assigned to resource " + request.resourceId()
            );
        }

        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        ResourceServiceEntity resourceService = ResourceServiceEntity.builder()
            .resource(resource)
            .service(service)
            .build();

        return resourceServiceRepository.save(resourceService);
    }

    @Override
    public ResourceServiceEntity getResourceServiceById(
        BigInteger resourceId,
        BigInteger serviceId
    ) {
        return resourceServiceRepository.findById(new ResourceServiceId(resourceId, serviceId))
            .orElseThrow(() -> new ResourceServiceNotFoundException(
                "Service " + serviceId + " is not assigned to resource " + resourceId
            ));
    }

    @Override
    public List<ResourceServiceEntity> getAllResourceServices() {
        return resourceServiceRepository.findAll();
    }

    @Override
    public List<ResourceServiceEntity> getResourceServicesByResourceId(BigInteger resourceId) {
        return resourceServiceRepository.findByResourceResourceId(resourceId);
    }

    // Lets the resource list fetch every assignment in one call instead of one per row.
    @Override
    public List<ResourceServiceEntity> getResourceServicesByOrganizationId(BigInteger organizationId) {
        return resourceServiceRepository.findByResourceOrganizationOrganizationId(organizationId);
    }

    @Override
    public void deleteResourceService(
        BigInteger resourceId,
        BigInteger serviceId
    ) {
        resourceServiceRepository.delete(getResourceServiceById(resourceId, serviceId));
    }
}
