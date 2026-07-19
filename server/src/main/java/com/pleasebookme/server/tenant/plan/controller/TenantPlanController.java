package com.pleasebookme.server.tenant.plan.controller;

import com.pleasebookme.server.tenant.plan.dto.TenantPlanRequest;
import com.pleasebookme.server.tenant.plan.dto.TenantPlanResponse;
import com.pleasebookme.server.tenant.plan.service.TenantPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenant-plans")
@RequiredArgsConstructor
public class TenantPlanController {
    private final TenantPlanService tenantPlanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantPlanResponse createTenantPlan(@Valid @RequestBody TenantPlanRequest request) {
        return TenantPlanResponse.from(tenantPlanService.createTenantPlan(request));
    }

    @GetMapping("/{tenantPlanId}")
    public TenantPlanResponse getTenantPlan(@PathVariable BigInteger tenantPlanId) {
        return TenantPlanResponse.from(tenantPlanService.getTenantPlanById(tenantPlanId));
    }

    @GetMapping
    public List<TenantPlanResponse> getTenantPlans() {
        return tenantPlanService.getAllTenantPlans().stream()
            .map(TenantPlanResponse::from)
            .toList();
    }

    @PutMapping("/{tenantPlanId}")
    public TenantPlanResponse updateTenantPlan(
        @PathVariable BigInteger tenantPlanId,
        @Valid @RequestBody TenantPlanRequest request
    ) {
        return TenantPlanResponse.from(tenantPlanService.updateTenantPlan(tenantPlanId, request));
    }

    @DeleteMapping("/{tenantPlanId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTenantPlan(@PathVariable BigInteger tenantPlanId) {
        tenantPlanService.deleteTenantPlan(tenantPlanId);
    }
}
