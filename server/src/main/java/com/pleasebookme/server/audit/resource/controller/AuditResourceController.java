package com.pleasebookme.server.audit.resource.controller;

import com.pleasebookme.server.audit.resource.dto.AuditResourceRequest;
import com.pleasebookme.server.audit.resource.dto.AuditResourceResponse;
import com.pleasebookme.server.audit.resource.service.AuditResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-resources")
@RequiredArgsConstructor
public class AuditResourceController {
    private final AuditResourceService auditResourceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuditResourceResponse createAuditResource(@Valid @RequestBody AuditResourceRequest request) {
        return AuditResourceResponse.from(auditResourceService.createAuditResource(request));
    }

    @GetMapping("/{auditResourceId}")
    public AuditResourceResponse getAuditResource(@PathVariable BigInteger auditResourceId) {
        return AuditResourceResponse.from(auditResourceService.getAuditResourceById(auditResourceId));
    }

    @GetMapping
    public List<AuditResourceResponse> getAuditResources() {
        return auditResourceService.getAllAuditResources().stream()
            .map(AuditResourceResponse::from)
            .toList();
    }
}
