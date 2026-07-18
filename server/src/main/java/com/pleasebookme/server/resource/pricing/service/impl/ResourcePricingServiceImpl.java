package com.pleasebookme.server.resource.pricing.service.impl;

import com.pleasebookme.server.resource.pricing.dto.ResourcePricingRequest;
import com.pleasebookme.server.resource.pricing.entity.ResourcePricingEntity;
import com.pleasebookme.server.resource.pricing.exception.ResourcePricingNotFoundException;
import com.pleasebookme.server.resource.pricing.repository.ResourcePricingRepository;
import com.pleasebookme.server.resource.pricing.service.ResourcePricingService;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import com.pleasebookme.server.resource.resources.exception.ResourceNotFoundException;
import com.pleasebookme.server.resource.resources.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourcePricingServiceImpl implements ResourcePricingService {
    private final ResourcePricingRepository resourcePricingRepository;
    private final ResourceRepository resourceRepository;

    @Override
    public ResourcePricingEntity createResourcePricing(ResourcePricingRequest request) {
        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        ResourcePricingEntity.ResourcePricingEntityBuilder resourcePricing = ResourcePricingEntity.builder()
            .resource(resource)
            .price(request.price())
            .effectiveFrom(request.effectiveFrom())
            .effectiveUntil(request.effectiveUntil());

        if (request.currency() != null) resourcePricing.currency(request.currency());

        return resourcePricingRepository.save(resourcePricing.build());
    }

    @Override
    public ResourcePricingEntity getResourcePricingById(BigInteger resourcePricingId) {
        return resourcePricingRepository.findById(resourcePricingId)
            .orElseThrow(() -> new ResourcePricingNotFoundException("Resource pricing not found: " + resourcePricingId));
    }

    @Override
    public List<ResourcePricingEntity> getAllResourcePricings() {
        return resourcePricingRepository.findAll();
    }

    @Override
    public ResourcePricingEntity updateResourcePricing(
        BigInteger resourcePricingId,
        ResourcePricingRequest request
    ) {
        ResourcePricingEntity resourcePricing = getResourcePricingById(resourcePricingId);

        ResourceEntity resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + request.resourceId()));

        resourcePricing.setResource(resource);
        resourcePricing.setPrice(request.price());
        resourcePricing.setEffectiveFrom(request.effectiveFrom());
        resourcePricing.setEffectiveUntil(request.effectiveUntil());

        if (request.currency() != null) resourcePricing.setCurrency(request.currency());

        return resourcePricingRepository.save(resourcePricing);
    }

    @Override
    public void deleteResourcePricing(BigInteger resourcePricingId) {
        resourcePricingRepository.delete(getResourcePricingById(resourcePricingId));
    }
}
