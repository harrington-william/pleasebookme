package com.pleasebookme.server.audit.event.service;

import com.pleasebookme.server.audit.event.dto.AuditEventRequest;
import com.pleasebookme.server.audit.event.entity.AuditEventEntity;

import java.math.BigInteger;
import java.util.List;

public interface AuditEventService {
    AuditEventEntity createAuditEvent(AuditEventRequest request);

    AuditEventEntity getAuditEventById(BigInteger auditEventId);

    List<AuditEventEntity> getAllAuditEvents();
}
