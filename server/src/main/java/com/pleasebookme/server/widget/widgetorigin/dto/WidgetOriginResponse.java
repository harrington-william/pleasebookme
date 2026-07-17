package com.pleasebookme.server.widget.widgetorigin.dto;

import com.pleasebookme.server.widget.widgetorigin.entity.WidgetOriginEntity;

import java.math.BigInteger;
import java.time.Instant;

public record WidgetOriginResponse(
    BigInteger widgetOriginId,
    BigInteger widgetId,
    String origin,
    Boolean verified,
    BigInteger createdById,
    Instant createdAt
) {
    public static WidgetOriginResponse from(WidgetOriginEntity widgetOrigin) {
        return new WidgetOriginResponse(
            widgetOrigin.getWidgetOriginId(),
            widgetOrigin.getWidget().getWidgetId(),
            widgetOrigin.getOrigin(),
            widgetOrigin.getVerified(),
            widgetOrigin.getCreatedBy() != null ? widgetOrigin.getCreatedBy().getUserId() : null,
            widgetOrigin.getCreatedAt()
        );
    }
}
