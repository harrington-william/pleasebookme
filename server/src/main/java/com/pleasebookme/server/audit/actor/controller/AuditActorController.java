package com.pleasebookme.server.audit.actor.controller;

import com.pleasebookme.server.audit.actor.dto.AuditActorRequest;
import com.pleasebookme.server.audit.actor.dto.AuditActorResponse;
import com.pleasebookme.server.audit.actor.service.AuditActorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-actors")
@RequiredArgsConstructor
public class AuditActorController {
    private final AuditActorService auditActorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuditActorResponse createAuditActor(@Valid @RequestBody AuditActorRequest request) {
        return AuditActorResponse.from(auditActorService.createAuditActor(request));
    }

    @GetMapping("/{auditActorId}")
    public AuditActorResponse getAuditActor(@PathVariable BigInteger auditActorId) {
        return AuditActorResponse.from(auditActorService.getAuditActorById(auditActorId));
    }

    @GetMapping
    public List<AuditActorResponse> getAuditActors() {
        return auditActorService.getAllAuditActors().stream()
            .map(AuditActorResponse::from)
            .toList();
    }
}
