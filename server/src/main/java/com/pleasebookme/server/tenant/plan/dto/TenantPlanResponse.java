package com.pleasebookme.server.tenant.plan.dto;

import com.pleasebookme.server.global.enums.Currency;
import com.pleasebookme.server.tenant.plan.entity.TenantPlanEntity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public record TenantPlanResponse(
    BigInteger tenantPlanId,
    String code,
    String name,
    BigDecimal price,
    Currency currency,
    Integer maxUsers,
    Integer maxServices,
    Integer maxWidgets,
    Integer maxResources,
    Integer maxApiKeys,
    Instant createdAt,
    Instant updatedAt
) {
    public static TenantPlanResponse from(TenantPlanEntity plan) {
        return new TenantPlanResponse(
            plan.getTenantPlanId(),
            plan.getCode(),
            plan.getName(),
            plan.getPrice(),
            plan.getCurrency(),
            plan.getMaxUsers(),
            plan.getMaxServices(),
            plan.getMaxWidgets(),
            plan.getMaxResources(),
            plan.getMaxApiKeys(),
            plan.getCreatedAt(),
            plan.getUpdatedAt()
        );
    }
}
