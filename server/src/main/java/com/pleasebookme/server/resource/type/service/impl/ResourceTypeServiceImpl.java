package com.pleasebookme.server.resource.type.service.impl;

import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.resource.type.dto.ResourceTypeRequest;
import com.pleasebookme.server.resource.type.entity.ResourceTypeEntity;
import com.pleasebookme.server.resource.type.exception.ResourceTypeNotFoundException;
import com.pleasebookme.server.resource.type.repository.ResourceTypeRepository;
import com.pleasebookme.server.resource.type.service.ResourceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceTypeServiceImpl implements ResourceTypeService {
    private final ResourceTypeRepository resourceTypeRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    public ResourceTypeEntity createResourceType(ResourceTypeRequest request) {
        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + request.organizationId()));

        ResourceTypeEntity resourceType = ResourceTypeEntity.builder()
            .organization(organization)
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
    public List<ResourceTypeEntity> getAllResourceTypes() {
        return resourceTypeRepository.findAll();
    }

    @Override
    public ResourceTypeEntity updateResourceType(
        BigInteger resourceTypeId,
        ResourceTypeRequest request
    ) {
        ResourceTypeEntity resourceType = getResourceTypeById(resourceTypeId);

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + request.organizationId()));

        resourceType.setOrganization(organization);
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
