package com.pleasebookme.server.service.auth.dto;

import com.pleasebookme.server.global.enums.Locale;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "Missing username")
    String username,
    @NotBlank(message = "Missing password")
    String password,

    @NotBlank(message = "Missing email")
    String email,
    String phone,

    @NotBlank(message = "Missing name")
    String name,

    Locale locale,
    String timezone
) {}
