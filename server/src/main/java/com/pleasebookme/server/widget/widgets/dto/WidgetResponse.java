package com.pleasebookme.server.widget.widgets.dto;

import com.pleasebookme.server.widget.enums.WidgetStatus;
import com.pleasebookme.server.widget.enums.WidgetType;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record WidgetResponse(
    BigInteger widgetId,
    UUID widgetUid,
    BigInteger tenantId,
    String name,
    WidgetStatus status,
    WidgetType type,
    Boolean originValidation,
    String publicKey,
    String origin,
    Instant issuedAt,
    Instant expiresAt,
    Instant lastUsedAt,
    Instant createdAt,
    Instant updatedAt
) {
    public static WidgetResponse from(WidgetDetail detail) {
        var widget = detail.widget();

        return new WidgetResponse(
            widget.getWidgetId(),
            widget.getWidgetUid(),
            widget.getTenant().getTenantId(),
            widget.getName(),
            widget.getStatus(),
            widget.getType(),
            widget.getOriginValidation(),
            widget.getPublicKey(),
            detail.origin() != null ? detail.origin().getOrigin() : null,
            widget.getIssuedAt(),
            widget.getExpiresAt(),
            widget.getLastUsedAt(),
            widget.getCreatedAt(),
            widget.getUpdatedAt()
        );
    }
}
