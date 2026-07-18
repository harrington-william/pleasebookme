package com.pleasebookme.server.audit.event.dto;

import com.pleasebookme.server.audit.enums.AuditAction;
import com.pleasebookme.server.audit.enums.AuditDomain;
import com.pleasebookme.server.audit.enums.AuditStatus;
import com.pleasebookme.server.audit.enums.EventType;
import com.pleasebookme.server.audit.enums.Severity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record AuditEventRequest(
    @Size(max = 255)
    String correlationId,

    @Size(max = 255)
    String requestId,

    @Size(max = 255)
    String traceId,

    BigInteger tenantId,

    BigInteger organizationId,

    @NotNull
    BigInteger actorId,

    @NotNull
    AuditDomain eventDomain,

    @NotNull
    EventType eventType,

    @NotNull
    AuditAction action,

    @NotNull
    Severity severity,

    @NotNull
    AuditStatus status,

    Integer eventVersion
) {
}
