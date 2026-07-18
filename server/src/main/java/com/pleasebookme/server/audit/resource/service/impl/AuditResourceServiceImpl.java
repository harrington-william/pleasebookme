package com.pleasebookme.server.audit.resource.service.impl;

import com.pleasebookme.server.audit.event.entity.AuditEventEntity;
import com.pleasebookme.server.audit.event.exception.AuditEventNotFoundException;
import com.pleasebookme.server.audit.event.repository.AuditEventRepository;
import com.pleasebookme.server.audit.resource.dto.AuditResourceRequest;
import com.pleasebookme.server.audit.resource.entity.AuditResourceEntity;
import com.pleasebookme.server.audit.resource.exception.AuditResourceNotFoundException;
import com.pleasebookme.server.audit.resource.repository.AuditResourceRepository;
import com.pleasebookme.server.audit.resource.service.AuditResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditResourceServiceImpl implements AuditResourceService {
    private final AuditResourceRepository auditResourceRepository;
    private final AuditEventRepository auditEventRepository;

    @Override
    public AuditResourceEntity createAuditResource(AuditResourceRequest request) {
        AuditEventEntity event = auditEventRepository.findById(request.eventId())
            .orElseThrow(() -> new AuditEventNotFoundException("Audit event not found: " + request.eventId()));

        AuditResourceEntity auditResource = AuditResourceEntity.builder()
            .event(event)
            .resourceType(request.resourceType())
            .resourceUid(request.resourceUid())
            .resourceName(request.resourceName())
            .beforeSnapshot(request.beforeSnapshot())
            .afterSnapshot(request.afterSnapshot())
            .build();

        return auditResourceRepository.save(auditResource);
    }

    @Override
    public AuditResourceEntity getAuditResourceById(BigInteger auditResourceId) {
        return auditResourceRepository.findById(auditResourceId)
            .orElseThrow(() -> new AuditResourceNotFoundException(
                "Audit resource not found: " + auditResourceId
            ));
    }

    @Override
    public List<AuditResourceEntity> getAllAuditResources() {
        return auditResourceRepository.findAll();
    }
}
