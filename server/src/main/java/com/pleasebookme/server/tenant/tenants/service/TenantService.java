package com.pleasebookme.server.tenant.tenants.service;

import com.pleasebookme.server.tenant.tenants.dto.TenantRequest;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;

import java.math.BigInteger;
import java.util.List;

public interface TenantService {
    TenantEntity createTenant(TenantRequest request);

    TenantEntity getTenantById(BigInteger tenantId);

    List<TenantEntity> getAllTenants();

    TenantEntity updateTenant(
        BigInteger tenantId,
        TenantRequest request
    );

    void deleteTenant(BigInteger tenantId);
}
