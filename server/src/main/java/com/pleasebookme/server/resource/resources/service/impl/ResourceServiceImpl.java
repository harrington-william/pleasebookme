package com.pleasebookme.server.resource.resources.service.impl;

import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.resource.resources.dto.ResourceRequest;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import com.pleasebookme.server.resource.resources.exception.DuplicateResourceException;
import com.pleasebookme.server.resource.resources.exception.ResourceNotFoundException;
import com.pleasebookme.server.resource.resources.repository.ResourceRepository;
import com.pleasebookme.server.resource.resources.service.ResourceService;
import com.pleasebookme.server.resource.type.entity.ResourceTypeEntity;
import com.pleasebookme.server.resource.type.exception.ResourceTypeNotFoundException;
import com.pleasebookme.server.resource.type.repository.ResourceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final ResourceRepository resourceRepository;
    private final OrganizationRepository organizationRepository;
    private final ServiceRepository serviceRepository;
    private final ResourceTypeRepository resourceTypeRepository;

    @Override
    public ResourceEntity createResource(ResourceRequest request) {
        if (resourceRepository.existsByOrganizationOrganizationIdAndSlug(request.organizationId(), request.slug())) {
            throw new DuplicateResourceException("Slug already exists for organization: " + request.slug());
        }

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + request.organizationId()));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        ResourceTypeEntity resourceType = resourceTypeRepository.findById(request.resourceTypeId())
            .orElseThrow(() -> new ResourceTypeNotFoundException("Resource type not found: " + request.resourceTypeId()));

        ResourceEntity.ResourceEntityBuilder resource = ResourceEntity.builder()
            .organization(organization)
            .service(service)
            .resourceType(resourceType)
            .name(request.name())
            .slug(request.slug())
            .description(request.description())
            .capacity(request.capacity())
            .status(request.status());

        if (request.isBookable() != null) resource.isBookable(request.isBookable());
        if (request.isVirtual() != null) resource.isVirtual(request.isVirtual());

        return resourceRepository.save(resource.build());
    }

    @Override
    public ResourceEntity getResourceById(BigInteger resourceId) {
        return resourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));
    }

    @Override
    public List<ResourceEntity> getAllResources() {
        return resourceRepository.findAll();
    }

    @Override
    public ResourceEntity updateResource(
        BigInteger resourceId,
        ResourceRequest request
    ) {
        ResourceEntity resource = getResourceById(resourceId);

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + request.organizationId()));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        ResourceTypeEntity resourceType = resourceTypeRepository.findById(request.resourceTypeId())
            .orElseThrow(() -> new ResourceTypeNotFoundException("Resource type not found: " + request.resourceTypeId()));

        resource.setOrganization(organization);
        resource.setService(service);
        resource.setResourceType(resourceType);
        resource.setName(request.name());
        resource.setSlug(request.slug());
        resource.setDescription(request.description());
        resource.setCapacity(request.capacity());
        resource.setStatus(request.status());

        if (request.isBookable() != null) resource.setIsBookable(request.isBookable());
        if (request.isVirtual() != null) resource.setIsVirtual(request.isVirtual());

        return resourceRepository.save(resource);
    }

    @Override
    public void deleteResource(BigInteger resourceId) {
        resourceRepository.delete(getResourceById(resourceId));
    }
}
