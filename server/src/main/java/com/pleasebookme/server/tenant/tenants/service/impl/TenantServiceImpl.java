package com.pleasebookme.server.tenant.tenants.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.tenant.ecosystem.entity.EcosystemEntity;
import com.pleasebookme.server.tenant.ecosystem.exception.EcosystemNotFoundException;
import com.pleasebookme.server.tenant.ecosystem.repository.EcosystemRepository;
import com.pleasebookme.server.tenant.plan.entity.TenantPlanEntity;
import com.pleasebookme.server.tenant.plan.exception.TenantPlanNotFoundException;
import com.pleasebookme.server.tenant.plan.repository.TenantPlanRepository;
import com.pleasebookme.server.tenant.tenants.dto.TenantRequest;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.tenants.exception.DuplicateTenantException;
import com.pleasebookme.server.tenant.tenants.exception.TenantNotFoundException;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import com.pleasebookme.server.tenant.tenants.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
    private final TenantRepository tenantRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final EcosystemRepository ecosystemRepository;
    private final TenantPlanRepository tenantPlanRepository;

    @Override
    public TenantEntity createTenant(TenantRequest request) {
        if (tenantRepository.existsBySlug(request.slug())) {
            throw new DuplicateTenantException("Slug already exists: " + request.slug());
        }

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException(
                "Organization not found: " + request.organizationId()
            ));

        UserEntity ownerUser = userRepository.findById(request.ownerUserId())
            .orElseThrow(() -> new UserNotFoundException(
                "User not found: " + request.ownerUserId()
            ));

        EcosystemEntity ecosystem = ecosystemRepository.findById(request.ecosystemId())
            .orElseThrow(() -> new EcosystemNotFoundException(
                "Ecosystem not found: " + request.ecosystemId()
            ));

        TenantPlanEntity plan = tenantPlanRepository.findById(request.planId())
            .orElseThrow(() -> new TenantPlanNotFoundException(
                "Tenant plan not found: " + request.planId()
            ));

        TenantEntity.TenantEntityBuilder tenant = TenantEntity.builder()
            .organization(organization)
            .ownerUser(ownerUser)
            .ecosystem(ecosystem)
            .name(request.name())
            .slug(request.slug())
            .status(request.status())
            .plan(plan)
            .region(request.region())
            .maxUsers(request.maxUsers())
            .maxServices(request.maxServices())
            .maxWidgets(request.maxWidgets());

        if (request.defaultTimezone() != null) tenant.defaultTimezone(request.defaultTimezone());
        if (request.defaultLocale() != null) tenant.defaultLocale(request.defaultLocale());

        return tenantRepository.save(tenant.build());
    }

    @Override
    public TenantEntity getTenantById(BigInteger tenantId) {
        return tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantNotFoundException(
                "Tenant not found: " + tenantId
            ));
    }

    @Override
    public List<TenantEntity> getAllTenants() {
        return tenantRepository.findAll();
    }

    @Override
    public TenantEntity updateTenant(
        BigInteger tenantId,
        TenantRequest request
    ) {
        TenantEntity tenant = getTenantById(tenantId);

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException(
                "Organization not found: " + request.organizationId()
            ));

        UserEntity ownerUser = userRepository.findById(request.ownerUserId())
            .orElseThrow(() -> new UserNotFoundException(
                "User not found: " + request.ownerUserId()
            ));

        EcosystemEntity ecosystem = ecosystemRepository.findById(request.ecosystemId())
            .orElseThrow(() -> new EcosystemNotFoundException(
                "Ecosystem not found: " + request.ecosystemId()
            ));

        TenantPlanEntity plan = tenantPlanRepository.findById(request.planId())
            .orElseThrow(() -> new TenantPlanNotFoundException(
                "Tenant plan not found: " + request.planId()
            ));

        tenant.setOrganization(organization);
        tenant.setOwnerUser(ownerUser);
        tenant.setEcosystem(ecosystem);
        tenant.setName(request.name());
        tenant.setSlug(request.slug());
        tenant.setStatus(request.status());
        tenant.setPlan(plan);
        tenant.setRegion(request.region());
        tenant.setMaxUsers(request.maxUsers());
        tenant.setMaxServices(request.maxServices());
        tenant.setMaxWidgets(request.maxWidgets());

        if (request.defaultTimezone() != null) tenant.setDefaultTimezone(request.defaultTimezone());
        if (request.defaultLocale() != null) tenant.setDefaultLocale(request.defaultLocale());

        return tenantRepository.save(tenant);
    }

    @Override
    public void deleteTenant(BigInteger tenantId) {
        tenantRepository.delete(getTenantById(tenantId));
    }
}
