package com.pleasebookme.server.resource.type.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResourceTypeRequest(
    @NotBlank
    @Size(max = 100)
    String name,

    String description,

    @Size(max = 100)
    String icon
) {
}
