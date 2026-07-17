package com.pleasebookme.server.tenant.domain.service;

import com.pleasebookme.server.tenant.domain.dto.TenantDomainRequest;
import com.pleasebookme.server.tenant.domain.entity.TenantDomainEntity;

import java.math.BigInteger;
import java.util.List;

public interface TenantDomainService {
    TenantDomainEntity createTenantDomain(TenantDomainRequest request);

    TenantDomainEntity getTenantDomainById(BigInteger tenantDomainId);

    List<TenantDomainEntity> getAllTenantDomains();

    TenantDomainEntity updateTenantDomain(
        BigInteger tenantDomainId,
        TenantDomainRequest request
    );

    void deleteTenantDomain(BigInteger tenantDomainId);
}
