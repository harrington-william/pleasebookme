package com.pleasebookme.server.service.tenant.service.impl;

import com.pleasebookme.server.service.organization.service.CurrentOrganizationProvider;
import com.pleasebookme.server.service.tenant.service.CurrentTenantProvider;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.tenants.exception.TenantNotFoundException;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class CurrentTenantProviderImpl implements CurrentTenantProvider {
    private final TenantRepository tenantRepository;
    private final CurrentOrganizationProvider currentOrganizationProvider;

    @Override
    @Transactional(readOnly = true)
    public TenantEntity requireCurrent() {
        return requireByOrganizationId(currentOrganizationProvider.requireCurrent().organizationId());
    }

    @Override
    @Transactional(readOnly = true)
    public TenantEntity requireByOrganizationId(BigInteger organizationId) {
        return tenantRepository.findByOrganizationOrganizationId(organizationId)
            .orElseThrow(() -> new TenantNotFoundException(
                "Organization " + organizationId
                    + " has no tenant; it predates tenant provisioning (V141)"
            ));
    }
}
