package com.pleasebookme.server.audit.change.service.impl;

import com.pleasebookme.server.audit.change.dto.AuditChangeRequest;
import com.pleasebookme.server.audit.change.entity.AuditChangeEntity;
import com.pleasebookme.server.audit.change.exception.AuditChangeNotFoundException;
import com.pleasebookme.server.audit.change.repository.AuditChangeRepository;
import com.pleasebookme.server.audit.change.service.AuditChangeService;
import com.pleasebookme.server.audit.resource.entity.AuditResourceEntity;
import com.pleasebookme.server.audit.resource.exception.AuditResourceNotFoundException;
import com.pleasebookme.server.audit.resource.repository.AuditResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditChangeServiceImpl implements AuditChangeService {
    private final AuditChangeRepository auditChangeRepository;
    private final AuditResourceRepository auditResourceRepository;

    @Override
    public AuditChangeEntity createAuditChange(AuditChangeRequest request) {
        AuditResourceEntity resource = auditResourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new AuditResourceNotFoundException("Audit resource not found: " + request.resourceId()));

        AuditChangeEntity auditChange = AuditChangeEntity.builder()
            .resource(resource)
            .fieldName(request.fieldName())
            .oldValue(request.oldValue())
            .newValue(request.newValue())
            .build();

        return auditChangeRepository.save(auditChange);
    }

    @Override
    public AuditChangeEntity getAuditChangeById(BigInteger auditChangeId) {
        return auditChangeRepository.findById(auditChangeId)
            .orElseThrow(() -> new AuditChangeNotFoundException(
                "Audit change not found: " + auditChangeId
            ));
    }

    @Override
    public List<AuditChangeEntity> getAllAuditChanges() {
        return auditChangeRepository.findAll();
    }
}
