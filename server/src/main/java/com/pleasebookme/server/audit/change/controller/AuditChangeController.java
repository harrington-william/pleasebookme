package com.pleasebookme.server.audit.change.controller;

import com.pleasebookme.server.audit.change.dto.AuditChangeRequest;
import com.pleasebookme.server.audit.change.dto.AuditChangeResponse;
import com.pleasebookme.server.audit.change.service.AuditChangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-changes")
@RequiredArgsConstructor
public class AuditChangeController {
    private final AuditChangeService auditChangeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuditChangeResponse createAuditChange(@Valid @RequestBody AuditChangeRequest request) {
        return AuditChangeResponse.from(auditChangeService.createAuditChange(request));
    }

    @GetMapping("/{auditChangeId}")
    public AuditChangeResponse getAuditChange(@PathVariable BigInteger auditChangeId) {
        return AuditChangeResponse.from(auditChangeService.getAuditChangeById(auditChangeId));
    }

    @GetMapping
    public List<AuditChangeResponse> getAuditChanges() {
        return auditChangeService.getAllAuditChanges().stream()
            .map(AuditChangeResponse::from)
            .toList();
    }
}
