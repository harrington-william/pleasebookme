package com.pleasebookme.server.auth.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleRequest(
    @NotBlank
    @Size(max = 100)
    String name,

    String description
) {
}
