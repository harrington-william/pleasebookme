package com.pleasebookme.server.auth.rolepermission.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record RolePermissionRequest(
    @NotNull
    BigInteger roleId,

    @NotNull
    BigInteger permissionId
) {
}
