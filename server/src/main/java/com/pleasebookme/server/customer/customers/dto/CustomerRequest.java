package com.pleasebookme.server.customer.customers.dto;

import com.pleasebookme.server.global.enums.Locale;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;
import java.time.LocalDate;

public record CustomerRequest(
    @NotNull
    BigInteger tenantId,

    @NotNull
    BigInteger organizationId,

    @NotBlank
    @Size(max = 255)
    String email,

    @NotBlank
    @Size(max = 50)
    String phone,

    @NotBlank
    @Size(max = 255)
    String name,

    String avatarUrl,

    Locale locale,

    @Size(max = 100)
    String timezone,

    @NotNull
    LocalDate birthday,

    @Size(max = 20)
    String gender,

    @NotBlank
    @Size(max = 50)
    String status,

    Boolean marketingConsent,

    String notes
) {
}
