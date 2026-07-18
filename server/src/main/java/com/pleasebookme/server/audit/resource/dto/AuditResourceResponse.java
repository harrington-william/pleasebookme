package com.pleasebookme.server.audit.resource.dto;

import com.pleasebookme.server.audit.enums.ResourceType;
import com.pleasebookme.server.audit.resource.entity.AuditResourceEntity;
import tools.jackson.databind.JsonNode;

import java.math.BigInteger;
import java.time.Instant;

public record AuditResourceResponse(
    BigInteger auditResourceId,
    BigInteger eventId,
    ResourceType resourceType,
    String resourceUid,
    String resourceName,
    JsonNode beforeSnapshot,
    JsonNode afterSnapshot,
    Instant createdAt
) {
    public static AuditResourceResponse from(AuditResourceEntity auditResource) {
        return new AuditResourceResponse(
            auditResource.getAuditResourceId(),
            auditResource.getEvent().getAuditEventId(),
            auditResource.getResourceType(),
            auditResource.getResourceUid(),
            auditResource.getResourceName(),
            auditResource.getBeforeSnapshot(),
            auditResource.getAfterSnapshot(),
            auditResource.getCreatedAt()
        );
    }
}
