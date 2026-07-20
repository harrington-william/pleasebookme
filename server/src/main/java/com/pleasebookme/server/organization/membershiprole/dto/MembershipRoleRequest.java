package com.pleasebookme.server.organization.membershiprole.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record MembershipRoleRequest(
    @NotNull
    BigInteger membershipId,

    @NotNull
    BigInteger roleId
) {
}
