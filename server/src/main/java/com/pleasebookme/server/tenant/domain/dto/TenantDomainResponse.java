package com.pleasebookme.server.tenant.domain.dto;

import com.pleasebookme.server.tenant.domain.entity.TenantDomainEntity;

import java.math.BigInteger;
import java.time.Instant;

public record TenantDomainResponse(
    BigInteger tenantDomainId,
    BigInteger tenantId,
    String domain,
    Boolean verified,
    Boolean isPrimary,
    String verificationToken,
    Instant createdAt,
    Instant updatedAt
) {
    public static TenantDomainResponse from(TenantDomainEntity tenantDomain) {
        return new TenantDomainResponse(
            tenantDomain.getTenantDomainId(),
            tenantDomain.getTenant().getTenantId(),
            tenantDomain.getDomain(),
            tenantDomain.getVerified(),
            tenantDomain.getIsPrimary(),
            tenantDomain.getVerificationToken(),
            tenantDomain.getCreatedAt(),
            tenantDomain.getUpdatedAt()
        );
    }
}
