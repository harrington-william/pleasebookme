package com.pleasebookme.server.audit.event.controller;

import com.pleasebookme.server.audit.event.dto.AuditEventRequest;
import com.pleasebookme.server.audit.event.dto.AuditEventResponse;
import com.pleasebookme.server.audit.event.service.AuditEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-events")
@RequiredArgsConstructor
public class AuditEventController {
    private final AuditEventService auditEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuditEventResponse createAuditEvent(@Valid @RequestBody AuditEventRequest request) {
        return AuditEventResponse.from(auditEventService.createAuditEvent(request));
    }

    @GetMapping("/{auditEventId}")
    public AuditEventResponse getAuditEvent(@PathVariable BigInteger auditEventId) {
        return AuditEventResponse.from(auditEventService.getAuditEventById(auditEventId));
    }

    @GetMapping
    public List<AuditEventResponse> getAuditEvents() {
        return auditEventService.getAllAuditEvents().stream()
            .map(AuditEventResponse::from)
            .toList();
    }
}
