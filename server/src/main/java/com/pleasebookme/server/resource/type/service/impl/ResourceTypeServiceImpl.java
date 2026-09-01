package com.pleasebookme.server.resource.type.service.impl;

import com.pleasebookme.server.resource.type.dto.ResourceTypeRequest;
import com.pleasebookme.server.resource.type.entity.ResourceTypeEntity;
import com.pleasebookme.server.resource.type.exception.ResourceTypeNotFoundException;
import com.pleasebookme.server.resource.type.repository.ResourceTypeRepository;
import com.pleasebookme.server.resource.type.service.ResourceTypeService;
import com.pleasebookme.server.service.organization.context.OrganizationContext;
import com.pleasebookme.server.service.organization.service.CurrentOrganizationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceTypeServiceImpl implements ResourceTypeService {
    private final ResourceTypeRepository resourceTypeRepository;
    private final CurrentOrganizationProvider currentOrganizationProvider;

    @Override
    public ResourceTypeEntity createResourceType(ResourceTypeRequest request) {
        OrganizationContext organizationContext = currentOrganizationProvider.requireCurrent();

        ResourceTypeEntity resourceType = ResourceTypeEntity.builder()
            .organization(organizationContext.organization())
            .name(request.name())
            .description(request.description())
            .icon(request.icon())
            .build();

        return resourceTypeRepository.save(resourceType);
    }

    @Override
    public ResourceTypeEntity getResourceTypeById(BigInteger resourceTypeId) {
        return resourceTypeRepository.findById(resourceTypeId)
            .orElseThrow(() -> new ResourceTypeNotFoundException("Resource type not found: " + resourceTypeId));
    }

    @Override
    public List<ResourceTypeEntity> getResourceTypesByOrganizationId(BigInteger organizationId) {
        OrganizationContext organizationContext = currentOrganizationProvider.requireCurrent();

        if (!organizationContext.organizationId().equals(organizationId)) {
            return List.of();
        }

        return resourceTypeRepository.findByOrganizationOrganizationId(organizationId);
    }

    @Override
    public ResourceTypeEntity updateResourceType(
        BigInteger resourceTypeId,
        ResourceTypeRequest request
    ) {
        ResourceTypeEntity resourceType = getResourceTypeById(resourceTypeId);
        OrganizationContext organizationContext = currentOrganizationProvider.requireCurrent();

        if (!resourceType.getOrganization().getOrganizationId().equals(organizationContext.organizationId())) {
            throw new ResourceTypeNotFoundException("Resource type not found: " + resourceTypeId);
        }

        resourceType.setName(request.name());
        resourceType.setDescription(request.description());
        resourceType.setIcon(request.icon());

        return resourceTypeRepository.save(resourceType);
    }

    @Override
    public void deleteResourceType(BigInteger resourceTypeId) {
        resourceTypeRepository.delete(getResourceTypeById(resourceTypeId));
    }
}
