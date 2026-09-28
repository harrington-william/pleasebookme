package com.pleasebookme.server.security.identity.context.tenant.service;

import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;

import java.math.BigInteger;

public interface CurrentTenantProvider {
    TenantEntity requireCurrent();

    TenantEntity requireByOrganizationId(BigInteger organizationId);
}
