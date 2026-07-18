package com.pleasebookme.server.customer.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record CustomerActivityRequest(
    @NotNull
    BigInteger customerId,

    @NotBlank
    @Size(max = 50)
    String activityType,

    @NotBlank
    @Size(max = 50)
    String referenceType,

    @NotBlank
    @Size(max = 255)
    String referenceUid,

    String description
) {
}
