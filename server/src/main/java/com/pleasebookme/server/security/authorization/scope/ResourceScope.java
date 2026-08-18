package com.pleasebookme.server.security.authorization.scope;

import java.math.BigInteger;

public record ResourceScope(
    BigInteger organizationId,
    BigInteger tenantId,
    BigInteger ownerUserId
) {
    private static final ResourceScope UNSCOPED = new ResourceScope(
        null,
        null,
        null
    );

    public static ResourceScope unscoped() {
        return UNSCOPED;
    }

    public static ResourceScope ofOrganization(BigInteger organizationId) {
        return new ResourceScope(
            organizationId,
            null,
            null
        );
    }

    public static ResourceScope ofTenant(BigInteger tenantId) {
        return new ResourceScope(
            null,
            tenantId,
            null
        );
    }

    public static ResourceScope ofOwner(BigInteger ownerUserId) {
        return new ResourceScope(
            null,
            null,
            ownerUserId
        );
    }

    public boolean isUnscoped() {
        return organizationId == null && tenantId == null && ownerUserId == null;
    }

    public boolean matchesOrganization(BigInteger candidateOrganizationId) {
        return organizationId != null && organizationId.equals(candidateOrganizationId);
    }

    public boolean matchesTenant(BigInteger candidateTenantId) {
        return tenantId != null && tenantId.equals(candidateTenantId);
    }

    public boolean matchesOwner(BigInteger candidateUserId) {
        return ownerUserId != null && ownerUserId.equals(candidateUserId);
    }
}
