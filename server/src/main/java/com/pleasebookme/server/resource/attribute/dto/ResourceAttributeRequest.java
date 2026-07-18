package com.pleasebookme.server.resource.attribute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record ResourceAttributeRequest(
    @NotNull
    BigInteger resourceId,

    @NotBlank
    @Size(max = 100)
    String key,

    @NotBlank
    String value
) {
}
