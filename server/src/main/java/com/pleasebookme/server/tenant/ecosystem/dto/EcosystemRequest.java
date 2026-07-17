package com.pleasebookme.server.tenant.ecosystem.dto;

import com.pleasebookme.server.tenant.enums.EcosystemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EcosystemRequest(
    @NotBlank
    @Size(max = 50)
    String code,

    @NotBlank
    @Size(max = 100)
    String name,

    @NotBlank
    String description,

    @NotBlank
    @Size(max = 100)
    String icon,

    EcosystemStatus status
) {
}
