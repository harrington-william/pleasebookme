package com.pleasebookme.server.tenant.plan.service.impl;

import com.pleasebookme.server.tenant.plan.dto.TenantPlanRequest;
import com.pleasebookme.server.tenant.plan.entity.TenantPlanEntity;
import com.pleasebookme.server.tenant.plan.exception.DuplicateTenantPlanException;
import com.pleasebookme.server.tenant.plan.exception.TenantPlanNotFoundException;
import com.pleasebookme.server.tenant.plan.repository.TenantPlanRepository;
import com.pleasebookme.server.tenant.plan.service.TenantPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantPlanServiceImpl implements TenantPlanService {
    private final TenantPlanRepository tenantPlanRepository;

    @Override
    public TenantPlanEntity createTenantPlan(TenantPlanRequest request) {
        if (tenantPlanRepository.existsByCode(request.code())) {
            throw new DuplicateTenantPlanException("Code already exists: " + request.code());
        }

        TenantPlanEntity.TenantPlanEntityBuilder plan = TenantPlanEntity.builder()
            .code(request.code())
            .name(request.name())
            .price(request.price())
            .maxUsers(request.maxUsers())
            .maxServices(request.maxServices())
            .maxWidgets(request.maxWidgets())
            .maxResources(request.maxResources())
            .maxApiKeys(request.maxApiKeys());

        if (request.currency() != null) plan.currency(request.currency());

        return tenantPlanRepository.save(plan.build());
    }

    @Override
    public TenantPlanEntity getTenantPlanById(BigInteger tenantPlanId) {
        return tenantPlanRepository.findById(tenantPlanId)
            .orElseThrow(() -> new TenantPlanNotFoundException(
                "Tenant plan not found: " + tenantPlanId
            ));
    }

    @Override
    public List<TenantPlanEntity> getAllTenantPlans() {
        return tenantPlanRepository.findAll();
    }

    @Override
    public TenantPlanEntity updateTenantPlan(
        BigInteger tenantPlanId,
        TenantPlanRequest request
    ) {
        TenantPlanEntity plan = getTenantPlanById(tenantPlanId);

        plan.setCode(request.code());
        plan.setName(request.name());
        plan.setPrice(request.price());
        plan.setMaxUsers(request.maxUsers());
        plan.setMaxServices(request.maxServices());
        plan.setMaxWidgets(request.maxWidgets());
        plan.setMaxResources(request.maxResources());
        plan.setMaxApiKeys(request.maxApiKeys());

        if (request.currency() != null) plan.setCurrency(request.currency());

        return tenantPlanRepository.save(plan);
    }

    @Override
    public void deleteTenantPlan(BigInteger tenantPlanId) {
        tenantPlanRepository.delete(getTenantPlanById(tenantPlanId));
    }
}
