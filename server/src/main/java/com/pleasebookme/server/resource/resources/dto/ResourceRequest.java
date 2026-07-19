package com.pleasebookme.server.resource.resources.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record ResourceRequest(
    @NotNull
    BigInteger organizationId,

    @NotNull
    BigInteger serviceId,

    @NotNull
    BigInteger resourceTypeId,

    @NotBlank
    @Size(max = 255)
    String name,

    @NotBlank
    @Size(max = 255)
    String slug,

    @NotBlank
    String description,

    @NotNull
    Integer capacity,

    @NotBlank
    @Size(max = 50)
    String status,

    Boolean isBookable,

    Boolean isVirtual
) {
}
