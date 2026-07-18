package com.pleasebookme.server.resource.attribute.service.impl;

import com.pleasebookme.server.resource.attribute.dto.ResourceAttributeRequest;
import com.pleasebookme.server.resource.attribute.entity.ResourceAttributesEntity;
import com.pleasebookme.server.resource.attribute.exception.DuplicateResourceAttributeException;
import com.pleasebookme.server.resource.attribute.exception.ResourceAttributeNotFoundException;
import com.pleasebookme.server.resource.attribute.id.ResourceAttributesId;
import com.pleasebookme.server.resource.attribute.repository.ResourceAttributeRepository;
import com.pleasebookme.server.resource.attribute.service.ResourceAttributeService;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import com.pleasebookme.server.resource.resources.exception.ResourceNotFoundException;
import com.pleasebookme.server.resource.resources.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceAttributeServiceImpl implements ResourceAttributeService {
    private final ResourceAttributeRepository resourceAttributeRepository;
    private final ResourceRepository resourceRepository;

    @Override
    public ResourceAttributesEntity createResourceAttribute(ResourceAttributeRequest request) {
        ResourceAttributesId resourceAttributesId = new ResourceAttributesId(request.resourceId(), request.key());

        if (resourceAttributeRepository.existsById(resourceAttributesId)) {
            throw new DuplicateResourceAttributeException(
                "Attribute " + request.key() + " already exists for resource " + request.resourceId()
            );
        }

        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        ResourceAttributesEntity resourceAttribute = ResourceAttributesEntity.builder()
            .resourceAttributesId(resourceAttributesId)
            .resource(resource)
            .value(request.value())
            .build();

        return resourceAttributeRepository.save(resourceAttribute);
    }

    @Override
    public ResourceAttributesEntity getResourceAttributeById(
        BigInteger resourceId,
        String key
    ) {
        return resourceAttributeRepository.findById(new ResourceAttributesId(resourceId, key))
            .orElseThrow(() -> new ResourceAttributeNotFoundException(
                "Attribute " + key + " not found for resource " + resourceId
            ));
    }

    @Override
    public List<ResourceAttributesEntity> getAllResourceAttributes() {
        return resourceAttributeRepository.findAll();
    }

    @Override
    public ResourceAttributesEntity updateResourceAttribute(
        BigInteger resourceId,
        String key,
        ResourceAttributeRequest request
    ) {
        ResourceAttributesEntity resourceAttribute = getResourceAttributeById(resourceId, key);
        resourceAttribute.setValue(request.value());

        return resourceAttributeRepository.save(resourceAttribute);
    }

    @Override
    public void deleteResourceAttribute(
        BigInteger resourceId,
        String key
    ) {
        resourceAttributeRepository.delete(getResourceAttributeById(resourceId, key));
    }
}
