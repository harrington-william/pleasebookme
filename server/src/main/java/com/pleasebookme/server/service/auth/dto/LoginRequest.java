package com.pleasebookme.server.service.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Missing username")
    String username,

    @NotBlank(message = "Missing password")
    String password
) {}
