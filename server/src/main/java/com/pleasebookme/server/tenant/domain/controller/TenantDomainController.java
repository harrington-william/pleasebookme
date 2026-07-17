package com.pleasebookme.server.tenant.domain.controller;

import com.pleasebookme.server.tenant.domain.dto.TenantDomainRequest;
import com.pleasebookme.server.tenant.domain.dto.TenantDomainResponse;
import com.pleasebookme.server.tenant.domain.service.TenantDomainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenant-domains")
@RequiredArgsConstructor
public class TenantDomainController {
    private final TenantDomainService tenantDomainService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantDomainResponse createTenantDomain(@Valid @RequestBody TenantDomainRequest request) {
        return TenantDomainResponse.from(tenantDomainService.createTenantDomain(request));
    }

    @GetMapping("/{tenantDomainId}")
    public TenantDomainResponse getTenantDomain(@PathVariable BigInteger tenantDomainId) {
        return TenantDomainResponse.from(tenantDomainService.getTenantDomainById(tenantDomainId));
    }

    @GetMapping
    public List<TenantDomainResponse> getTenantDomains() {
        return tenantDomainService.getAllTenantDomains().stream()
            .map(TenantDomainResponse::from)
            .toList();
    }

    @PutMapping("/{tenantDomainId}")
    public TenantDomainResponse updateTenantDomain(
        @PathVariable BigInteger tenantDomainId,
        @Valid @RequestBody TenantDomainRequest request
    ) {
        return TenantDomainResponse.from(tenantDomainService.updateTenantDomain(tenantDomainId, request));
    }

    @DeleteMapping("/{tenantDomainId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTenantDomain(@PathVariable BigInteger tenantDomainId) {
        tenantDomainService.deleteTenantDomain(tenantDomainId);
    }
}
