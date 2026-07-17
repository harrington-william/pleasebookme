package com.pleasebookme.server.tenant.plan.service;

import com.pleasebookme.server.tenant.plan.dto.TenantPlanRequest;
import com.pleasebookme.server.tenant.plan.entity.TenantPlanEntity;

import java.math.BigInteger;
import java.util.List;

public interface TenantPlanService {
    TenantPlanEntity createTenantPlan(TenantPlanRequest request);

    TenantPlanEntity getTenantPlanById(BigInteger tenantPlanId);

    List<TenantPlanEntity> getAllTenantPlans();

    TenantPlanEntity updateTenantPlan(
        BigInteger tenantPlanId,
        TenantPlanRequest request
    );

    void deleteTenantPlan(BigInteger tenantPlanId);
}
