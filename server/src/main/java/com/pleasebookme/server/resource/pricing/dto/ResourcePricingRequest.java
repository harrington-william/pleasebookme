package com.pleasebookme.server.resource.pricing.dto;

import com.pleasebookme.server.global.enums.Currency;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public record ResourcePricingRequest(
    @NotNull
    BigInteger resourceId,

    @NotNull
    BigDecimal price,

    Currency currency,

    @NotNull
    Instant effectiveFrom,

    Instant effectiveUntil
) {
}
