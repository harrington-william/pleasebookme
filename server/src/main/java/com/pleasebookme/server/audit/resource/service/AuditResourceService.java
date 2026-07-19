package com.pleasebookme.server.audit.resource.service;

import com.pleasebookme.server.audit.resource.dto.AuditResourceRequest;
import com.pleasebookme.server.audit.resource.entity.AuditResourceEntity;

import java.math.BigInteger;
import java.util.List;

public interface AuditResourceService {
    AuditResourceEntity createAuditResource(AuditResourceRequest request);

    AuditResourceEntity getAuditResourceById(BigInteger auditResourceId);

    List<AuditResourceEntity> getAllAuditResources();
}
