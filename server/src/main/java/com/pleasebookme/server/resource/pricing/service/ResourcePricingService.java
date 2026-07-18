package com.pleasebookme.server.resource.pricing.service;

import com.pleasebookme.server.resource.pricing.dto.ResourcePricingRequest;
import com.pleasebookme.server.resource.pricing.entity.ResourcePricingEntity;

import java.math.BigInteger;
import java.util.List;

public interface ResourcePricingService {
    ResourcePricingEntity createResourcePricing(ResourcePricingRequest request);

    ResourcePricingEntity getResourcePricingById(BigInteger resourcePricingId);

    List<ResourcePricingEntity> getAllResourcePricings();

    ResourcePricingEntity updateResourcePricing(
        BigInteger resourcePricingId,
        ResourcePricingRequest request
    );

    void deleteResourcePricing(BigInteger resourcePricingId);
}
