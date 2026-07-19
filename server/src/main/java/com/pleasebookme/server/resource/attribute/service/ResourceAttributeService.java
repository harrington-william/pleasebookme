package com.pleasebookme.server.resource.attribute.service;

import com.pleasebookme.server.resource.attribute.dto.ResourceAttributeRequest;
import com.pleasebookme.server.resource.attribute.entity.ResourceAttributesEntity;

import java.math.BigInteger;
import java.util.List;

public interface ResourceAttributeService {
    ResourceAttributesEntity createResourceAttribute(ResourceAttributeRequest request);

    ResourceAttributesEntity getResourceAttributeById(
        BigInteger resourceId,
        String key
    );

    List<ResourceAttributesEntity> getAllResourceAttributes();

    ResourceAttributesEntity updateResourceAttribute(
        BigInteger resourceId,
        String key,
        ResourceAttributeRequest request
    );

    void deleteResourceAttribute(
        BigInteger resourceId,
        String key
    );
}
