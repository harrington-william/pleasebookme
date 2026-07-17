package com.pleasebookme.server.tenant.tenants.dto;

import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.tenant.enums.TenantRegion;
import com.pleasebookme.server.tenant.enums.TenantStatus;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
    BigInteger tenantId,
    UUID tenantUid,
    BigInteger organizationId,
    BigInteger ownerUserId,
    BigInteger ecosystemId,
    String name,
    String slug,
    TenantStatus status,
    BigInteger planId,
    TenantRegion region,
    String defaultTimezone,
    Locale defaultLocale,
    Integer maxUsers,
    Integer maxServices,
    Integer maxWidgets,
    Instant createdAt,
    Instant updatedAt
) {
    public static TenantResponse from(TenantEntity tenant) {
        return new TenantResponse(
            tenant.getTenantId(),
            tenant.getTenantUid(),
            tenant.getOrganization().getOrganizationId(),
            tenant.getOwnerUser().getUserId(),
            tenant.getEcosystem().getEcosystemId(),
            tenant.getName(),
            tenant.getSlug(),
            tenant.getStatus(),
            tenant.getPlan().getTenantPlanId(),
            tenant.getRegion(),
            tenant.getDefaultTimezone(),
            tenant.getDefaultLocale(),
            tenant.getMaxUsers(),
            tenant.getMaxServices(),
            tenant.getMaxWidgets(),
            tenant.getCreatedAt(),
            tenant.getUpdatedAt()
        );
    }
}
