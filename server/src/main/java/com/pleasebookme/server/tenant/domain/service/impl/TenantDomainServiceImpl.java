package com.pleasebookme.server.tenant.domain.service.impl;

import com.pleasebookme.server.tenant.domain.dto.TenantDomainRequest;
import com.pleasebookme.server.tenant.domain.entity.TenantDomainEntity;
import com.pleasebookme.server.tenant.domain.exception.DuplicateTenantDomainException;
import com.pleasebookme.server.tenant.domain.exception.TenantDomainNotFoundException;
import com.pleasebookme.server.tenant.domain.repository.TenantDomainRepository;
import com.pleasebookme.server.tenant.domain.service.TenantDomainService;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.tenants.exception.TenantNotFoundException;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantDomainServiceImpl implements TenantDomainService {
    private final TenantDomainRepository tenantDomainRepository;
    private final TenantRepository tenantRepository;

    @Override
    public TenantDomainEntity createTenantDomain(TenantDomainRequest request) {
        if (tenantDomainRepository.existsByDomain(request.domain())) {
            throw new DuplicateTenantDomainException("Domain already exists: " + request.domain());
        }

        TenantEntity tenant = tenantRepository.findById(request.tenantId())
            .orElseThrow(() -> new TenantNotFoundException(
                "Tenant not found: " + request.tenantId()
            ));

        TenantDomainEntity.TenantDomainEntityBuilder tenantDomain = TenantDomainEntity.builder()
            .tenant(tenant)
            .domain(request.domain())
            .verificationToken(request.verificationToken());

        if (request.verified() != null) tenantDomain.verified(request.verified());
        if (request.isPrimary() != null) tenantDomain.isPrimary(request.isPrimary());

        return tenantDomainRepository.save(tenantDomain.build());
    }

    @Override
    public TenantDomainEntity getTenantDomainById(BigInteger tenantDomainId) {
        return tenantDomainRepository.findById(tenantDomainId)
            .orElseThrow(() -> new TenantDomainNotFoundException(
                "Tenant domain not found: " + tenantDomainId
            ));
    }

    @Override
    public List<TenantDomainEntity> getAllTenantDomains() {
        return tenantDomainRepository.findAll();
    }

    @Override
    public TenantDomainEntity updateTenantDomain(
        BigInteger tenantDomainId,
        TenantDomainRequest request
    ) {
        TenantDomainEntity tenantDomain = getTenantDomainById(tenantDomainId);

        TenantEntity tenant = tenantRepository.findById(request.tenantId())
            .orElseThrow(() -> new TenantNotFoundException(
                "Tenant not found: " + request.tenantId()
            ));

        tenantDomain.setTenant(tenant);
        tenantDomain.setDomain(request.domain());
        tenantDomain.setVerificationToken(request.verificationToken());

        if (request.verified() != null) tenantDomain.setVerified(request.verified());
        if (request.isPrimary() != null) tenantDomain.setIsPrimary(request.isPrimary());

        return tenantDomainRepository.save(tenantDomain);
    }

    @Override
    public void deleteTenantDomain(BigInteger tenantDomainId) {
        tenantDomainRepository.delete(getTenantDomainById(tenantDomainId));
    }
}
