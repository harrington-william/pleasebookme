package com.pleasebookme.server.widget.widgets.dto;

import com.pleasebookme.server.widget.enums.WidgetStatus;
import com.pleasebookme.server.widget.enums.WidgetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.time.Instant;

public record WidgetRequest(
    @NotNull
    BigInteger tenantId,

    @NotBlank
    String name,

    WidgetStatus status,

    WidgetType type,

    Boolean originValidation,

    @NotBlank
    String publicKey,

    @NotBlank
    String secretKey,

    @NotNull
    Instant expiresAt,

    Instant lastUsedAt
) {
}
