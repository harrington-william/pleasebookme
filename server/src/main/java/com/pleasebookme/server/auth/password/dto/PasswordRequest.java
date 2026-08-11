package com.pleasebookme.server.auth.password.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record PasswordRequest(
    @NotNull
    BigInteger userId,

    @NotBlank
    String password
) {
}
