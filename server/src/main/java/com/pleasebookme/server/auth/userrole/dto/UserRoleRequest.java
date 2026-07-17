package com.pleasebookme.server.auth.userrole.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record UserRoleRequest(
    @NotNull
    BigInteger userId,

    @NotNull
    BigInteger roleId
) {
}
