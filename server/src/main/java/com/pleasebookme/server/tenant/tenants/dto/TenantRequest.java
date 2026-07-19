package com.pleasebookme.server.tenant.tenants.dto;

import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.tenant.enums.TenantRegion;
import com.pleasebookme.server.tenant.enums.TenantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record TenantRequest(
    @NotNull
    BigInteger organizationId,

    @NotNull
    BigInteger ownerUserId,

    @NotNull
    BigInteger ecosystemId,

    @NotBlank
    @Size(max = 255)
    String name,

    @NotBlank
    @Size(max = 255)
    String slug,

    @NotNull
    TenantStatus status,

    @NotNull
    BigInteger planId,

    @NotNull
    TenantRegion region,

    @Size(max = 100)
    String defaultTimezone,

    Locale defaultLocale,

    @NotNull
    Integer maxUsers,

    @NotNull
    Integer maxServices,

    @NotNull
    Integer maxWidgets
) {
}
