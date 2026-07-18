package com.pleasebookme.server.audit.event.dto;

import com.pleasebookme.server.audit.enums.AuditAction;
import com.pleasebookme.server.audit.enums.AuditDomain;
import com.pleasebookme.server.audit.enums.AuditStatus;
import com.pleasebookme.server.audit.enums.EventType;
import com.pleasebookme.server.audit.enums.Severity;
import com.pleasebookme.server.audit.event.entity.AuditEventEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
    BigInteger auditEventId,
    UUID auditEventUid,
    String correlationId,
    String requestId,
    String traceId,
    BigInteger tenantId,
    BigInteger organizationId,
    BigInteger actorId,
    AuditDomain eventDomain,
    EventType eventType,
    AuditAction action,
    Severity severity,
    AuditStatus status,
    Integer eventVersion,
    Instant occurredAt,
    Instant createdAt
) {
    public static AuditEventResponse from(AuditEventEntity auditEvent) {
        return new AuditEventResponse(
            auditEvent.getAuditEventId(),
            auditEvent.getAuditEventUid(),
            auditEvent.getCorrelationId(),
            auditEvent.getRequestId(),
            auditEvent.getTraceId(),
            auditEvent.getTenant() != null ? auditEvent.getTenant().getTenantId() : null,
            auditEvent.getOrganization() != null ? auditEvent.getOrganization().getOrganizationId() : null,
            auditEvent.getActor().getAuditActorId(),
            auditEvent.getEventDomain(),
            auditEvent.getEventType(),
            auditEvent.getAction(),
            auditEvent.getSeverity(),
            auditEvent.getStatus(),
            auditEvent.getEventVersion(),
            auditEvent.getOccurredAt(),
            auditEvent.getCreatedAt()
        );
    }
}
