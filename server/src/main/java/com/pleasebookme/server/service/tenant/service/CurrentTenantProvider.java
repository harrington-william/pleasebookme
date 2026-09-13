package com.pleasebookme.server.service.tenant.service;

import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;

import java.math.BigInteger;

public interface CurrentTenantProvider {
    TenantEntity requireCurrent();

    TenantEntity requireByOrganizationId(BigInteger organizationId);
}
