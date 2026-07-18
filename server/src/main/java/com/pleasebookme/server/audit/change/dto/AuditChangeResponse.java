package com.pleasebookme.server.audit.change.dto;

import com.pleasebookme.server.audit.change.entity.AuditChangeEntity;

import java.math.BigInteger;
import java.time.Instant;

public record AuditChangeResponse(
    BigInteger auditChangeId,
    BigInteger resourceId,
    String fieldName,
    String oldValue,
    String newValue,
    Instant occurredAt
) {
    public static AuditChangeResponse from(AuditChangeEntity auditChange) {
        return new AuditChangeResponse(
            auditChange.getAuditChangeId(),
            auditChange.getResource().getAuditResourceId(),
            auditChange.getFieldName(),
            auditChange.getOldValue(),
            auditChange.getNewValue(),
            auditChange.getOccurredAt()
        );
    }
}
