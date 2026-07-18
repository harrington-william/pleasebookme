package com.pleasebookme.server.resource.pricing.dto;

import com.pleasebookme.server.global.enums.Currency;
import com.pleasebookme.server.resource.pricing.entity.ResourcePricingEntity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public record ResourcePricingResponse(
    BigInteger resourcePricingId,
    BigInteger resourceId,
    BigDecimal price,
    Currency currency,
    Instant effectiveFrom,
    Instant effectiveUntil,
    Instant createdAt,
    Instant updatedAt
) {
    public static ResourcePricingResponse from(ResourcePricingEntity resourcePricing) {
        return new ResourcePricingResponse(
            resourcePricing.getResourcePricingId(),
            resourcePricing.getResource().getResourceId(),
            resourcePricing.getPrice(),
            resourcePricing.getCurrency(),
            resourcePricing.getEffectiveFrom(),
            resourcePricing.getEffectiveUntil(),
            resourcePricing.getCreatedAt(),
            resourcePricing.getUpdatedAt()
        );
    }
}
