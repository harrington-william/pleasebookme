package com.pleasebookme.server.organization.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record ProfileRequest(
    @NotNull
    BigInteger userId,

    @NotNull
    BigInteger organizationId,

    @NotBlank
    @Size(max = 100)
    String username
) {
}
