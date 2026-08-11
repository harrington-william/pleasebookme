package com.pleasebookme.server.service.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleHandoffRequest(
    @NotBlank(message = "Missing handoff code")
    String code
) {}
