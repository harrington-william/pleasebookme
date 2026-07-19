package com.pleasebookme.server.widget.widgets.dto;

import com.pleasebookme.server.widget.enums.WidgetStatus;
import com.pleasebookme.server.widget.enums.WidgetType;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record WidgetResponse(
    BigInteger widgetId,
    UUID widgetUid,
    BigInteger tenantId,
    BigInteger serviceId,
    String name,
    WidgetStatus status,
    WidgetType type,
    Boolean originValidation,
    String publicKey,
    Instant issuedAt,
    Instant expiresAt,
    Instant lastUsedAt,
    Instant createdAt,
    Instant updatedAt
) {
    public static WidgetResponse from(WidgetEntity widget) {
        return new WidgetResponse(
            widget.getWidgetId(),
            widget.getWidgetUid(),
            widget.getTenant().getTenantId(),
            widget.getService().getServiceId(),
            widget.getName(),
            widget.getStatus(),
            widget.getType(),
            widget.getOriginValidation(),
            widget.getPublicKey(),
            widget.getIssuedAt(),
            widget.getExpiresAt(),
            widget.getLastUsedAt(),
            widget.getCreatedAt(),
            widget.getUpdatedAt()
        );
    }
}
