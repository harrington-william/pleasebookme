package com.pleasebookme.server.resource.resources.service.impl;

import com.pleasebookme.server.resource.enums.ResourceStatus;
import com.pleasebookme.server.resource.resources.dto.ResourceFilter;
import com.pleasebookme.server.resource.resources.dto.ResourceRequest;
import com.pleasebookme.server.resource.resources.dto.ResourceStatsResponse;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import com.pleasebookme.server.resource.resources.exception.DuplicateResourceException;
import com.pleasebookme.server.resource.resources.exception.ResourceNotFoundException;
import com.pleasebookme.server.resource.resources.repository.ResourceRepository;
import com.pleasebookme.server.resource.resources.service.ResourceService;
import com.pleasebookme.server.resource.resources.specification.ResourceSort;
import com.pleasebookme.server.resource.resources.specification.ResourceSpecifications;
import com.pleasebookme.server.resource.type.entity.ResourceTypeEntity;
import com.pleasebookme.server.resource.type.exception.ResourceTypeNotFoundException;
import com.pleasebookme.server.resource.type.repository.ResourceTypeRepository;
import com.pleasebookme.server.service.organization.context.OrganizationContext;
import com.pleasebookme.server.service.organization.service.CurrentOrganizationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final ResourceRepository resourceRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final CurrentOrganizationProvider currentOrganizationProvider;

    @Override
    public ResourceEntity createResource(ResourceRequest request) {
        OrganizationContext organizationContext = currentOrganizationProvider.requireCurrent();

        if (resourceRepository.existsByOrganizationOrganizationIdAndSlug(organizationContext.organizationId(), request.slug())) {
            throw new DuplicateResourceException("Slug already exists for organization: " + request.slug());
        }

        ResourceTypeEntity resourceType = resourceTypeRepository.findById(request.resourceTypeId())
            .orElseThrow(() -> new ResourceTypeNotFoundException("Resource type not found: " + request.resourceTypeId()));

        ResourceEntity.ResourceEntityBuilder resource = ResourceEntity.builder()
            .organization(organizationContext.organization())
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
    public Page<ResourceEntity> getResourcesByOrganizationId(
        BigInteger organizationId,
        ResourceFilter filter,
        Pageable pageable
    ) {
        OrganizationContext organizationContext = currentOrganizationProvider.requireCurrent();
        Pageable safePageable = ResourceSort.sanitize(pageable);

        if (!organizationContext.organizationId().equals(organizationId)) {
            return Page.empty(safePageable);
        }

        ResourceFilter appliedFilter = filter != null ? filter : ResourceFilter.none();

        Specification<ResourceEntity> specification = Specification.allOf(
            ResourceSpecifications.hasOrganization(organizationId),
            ResourceSpecifications.hasResourceType(appliedFilter.resourceTypeId()),
            ResourceSpecifications.hasStatus(appliedFilter.status()),
            ResourceSpecifications.matchesText(appliedFilter.q())
        );

        return resourceRepository.findAll(specification, safePageable);
    }

    @Override
    public List<ResourceEntity> getResourceLookupByOrganizationId(BigInteger organizationId) {
        return resourceRepository.findByOrganizationOrganizationId(organizationId);
    }

    @Override
    public ResourceStatsResponse getResourceStatsByOrganizationId(BigInteger organizationId) {
        OrganizationContext organizationContext = currentOrganizationProvider.requireCurrent();

        if (!organizationContext.organizationId().equals(organizationId)) {
            return new ResourceStatsResponse(0, 0, 0, 0, 0);
        }

        return new ResourceStatsResponse(
            resourceRepository.countByOrganizationOrganizationId(organizationId),
            countByStatus(organizationId, ResourceStatus.ACTIVE),
            countByStatus(organizationId, ResourceStatus.INACTIVE),
            countByStatus(organizationId, ResourceStatus.MAINTENANCE),
            countByStatus(organizationId, ResourceStatus.RETIRED)
        );
    }

    private long countByStatus(
        BigInteger organizationId,
        ResourceStatus status
    ) {
        return resourceRepository.countByOrganizationOrganizationIdAndStatus(organizationId, status);
    }

    @Override
    public ResourceEntity updateResource(
        BigInteger resourceId,
        ResourceRequest request
    ) {
        ResourceEntity resource = getResourceById(resourceId);
        OrganizationContext organizationContext = currentOrganizationProvider.requireCurrent();

        if (!resource.getOrganization().getOrganizationId().equals(organizationContext.organizationId())) {
            throw new ResourceNotFoundException("Resource not found: " + resourceId);
        }

        ResourceTypeEntity resourceType = resourceTypeRepository.findById(request.resourceTypeId())
            .orElseThrow(() -> new ResourceTypeNotFoundException("Resource type not found: " + request.resourceTypeId()));

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
