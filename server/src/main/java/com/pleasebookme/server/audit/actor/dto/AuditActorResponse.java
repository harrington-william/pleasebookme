package com.pleasebookme.server.audit.actor.dto;

import com.pleasebookme.server.audit.actor.entity.AuditActorEntity;
import com.pleasebookme.server.audit.enums.AuditActorType;

import java.math.BigInteger;
import java.net.InetAddress;
import java.time.Instant;

public record AuditActorResponse(
    BigInteger auditActorId,
    AuditActorType actorType,
    String userUid,
    BigInteger membershipId,
    String widgetUid,
    String apiKeyUid,
    BigInteger attendeeId,
    String systemName,
    String displayName,
    String email,
    InetAddress ipAddress,
    String userAgent,
    Instant createdAt
) {
    public static AuditActorResponse from(AuditActorEntity auditActor) {
        return new AuditActorResponse(
            auditActor.getAuditActorId(),
            auditActor.getActorType(),
            auditActor.getUserUid(),
            auditActor.getMembershipId(),
            auditActor.getWidgetUid(),
            auditActor.getApiKeyUid(),
            auditActor.getAttendeeId(),
            auditActor.getSystemName(),
            auditActor.getDisplayName(),
            auditActor.getEmail(),
            auditActor.getIpAddress(),
            auditActor.getUserAgent(),
            auditActor.getCreatedAt()
        );
    }
}
