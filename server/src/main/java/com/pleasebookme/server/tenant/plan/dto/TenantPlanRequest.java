package com.pleasebookme.server.tenant.plan.dto;

import com.pleasebookme.server.global.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TenantPlanRequest(
    @NotBlank
    @Size(max = 50)
    String code,

    @NotBlank
    @Size(max = 100)
    String name,

    @NotNull
    BigDecimal price,

    Currency currency,

    @NotNull
    Integer maxUsers,

    @NotNull
    Integer maxServices,

    @NotNull
    Integer maxWidgets,

    @NotNull
    Integer maxResources,

    @NotNull
    Integer maxApiKeys
) {
}
