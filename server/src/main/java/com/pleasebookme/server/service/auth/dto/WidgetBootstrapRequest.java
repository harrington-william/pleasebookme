package com.pleasebookme.server.service.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record WidgetBootstrapRequest(
    @NotBlank(message = "Missing public key")
    String publicKey,

    @NotBlank(message = "Missing secret key")
    String secretKey,

    @NotBlank(message = "Missing origin")
    String origin
) {}
