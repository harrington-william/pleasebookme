package com.pleasebookme.server.service.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleSignInRequest(
    @NotBlank(message = "Missing token ID")
    String idToken
) {}
