package com.pleasebookme.server.tenant.tenants.controller;

import com.pleasebookme.server.tenant.tenants.dto.TenantRequest;
import com.pleasebookme.server.tenant.tenants.dto.TenantResponse;
import com.pleasebookme.server.tenant.tenants.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService tenantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantResponse createTenant(@Valid @RequestBody TenantRequest request) {
        return TenantResponse.from(tenantService.createTenant(request));
    }

    @GetMapping("/{tenantId}")
    public TenantResponse getTenant(@PathVariable BigInteger tenantId) {
        return TenantResponse.from(tenantService.getTenantById(tenantId));
    }

    @GetMapping
    public List<TenantResponse> getTenants() {
        return tenantService.getAllTenants().stream()
            .map(TenantResponse::from)
            .toList();
    }

    @PutMapping("/{tenantId}")
    public TenantResponse updateTenant(
        @PathVariable BigInteger tenantId,
        @Valid @RequestBody TenantRequest request
    ) {
        return TenantResponse.from(tenantService.updateTenant(tenantId, request));
    }

    @DeleteMapping("/{tenantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTenant(@PathVariable BigInteger tenantId) {
        tenantService.deleteTenant(tenantId);
    }
}
