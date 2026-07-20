package com.pleasebookme.server.organization.membership.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record MembershipRequest(
    @NotNull
    BigInteger organizationId,

    @NotNull
    BigInteger userId,

    Boolean accepted
) {
}
