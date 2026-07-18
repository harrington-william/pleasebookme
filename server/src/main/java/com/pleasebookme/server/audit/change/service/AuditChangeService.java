package com.pleasebookme.server.audit.change.service;

import com.pleasebookme.server.audit.change.dto.AuditChangeRequest;
import com.pleasebookme.server.audit.change.entity.AuditChangeEntity;

import java.math.BigInteger;
import java.util.List;

public interface AuditChangeService {
    AuditChangeEntity createAuditChange(AuditChangeRequest request);

    AuditChangeEntity getAuditChangeById(BigInteger auditChangeId);

    List<AuditChangeEntity> getAllAuditChanges();
}
