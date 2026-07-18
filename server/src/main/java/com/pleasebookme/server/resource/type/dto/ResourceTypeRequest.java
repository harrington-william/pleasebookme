package com.pleasebookme.server.resource.type.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record ResourceTypeRequest(
    @NotNull
    BigInteger organizationId,

    @NotBlank
    @Size(max = 100)
    String name,

    String description,

    @NotBlank
    @Size(max = 100)
    String icon
) {
}
