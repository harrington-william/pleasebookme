package com.pleasebookme.server.resource.resources.dto;

import com.pleasebookme.server.resource.enums.ResourceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record ResourceRequest(
    @NotNull
    BigInteger resourceTypeId,

    @NotBlank
    @Size(max = 255)
    String name,

    @NotBlank
    @Size(max = 255)
    String slug,

    String description,

    Integer capacity,

    @NotNull
    ResourceStatus status,

    Boolean isBookable,

    Boolean isVirtual
) {
}
