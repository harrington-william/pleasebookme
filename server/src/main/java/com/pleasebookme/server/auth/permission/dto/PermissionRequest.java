package com.pleasebookme.server.auth.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissionRequest(
    @NotBlank
    @Size(max = 100)
    String name,

    String description,

    @NotBlank
    @Size(max = 100)
    String resource,

    @NotBlank
    @Size(max = 100)
    String action,

    @NotBlank
    @Size(max = 200)
    String slug
) {
}
