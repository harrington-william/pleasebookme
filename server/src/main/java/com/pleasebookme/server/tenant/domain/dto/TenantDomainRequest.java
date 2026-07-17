package com.pleasebookme.server.tenant.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record TenantDomainRequest(
    @NotNull
    BigInteger tenantId,

    @NotBlank
    @Size(max = 255)
    String domain,

    Boolean verified,

    Boolean isPrimary,

    @NotBlank
    String verificationToken
) {
}
