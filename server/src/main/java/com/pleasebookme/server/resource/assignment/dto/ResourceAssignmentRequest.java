package com.pleasebookme.server.resource.assignment.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.time.Instant;

public record ResourceAssignmentRequest(
    @NotNull
    BigInteger resourceId,

    @NotNull
    BigInteger membershipId,

    Instant releasedAt,

    BigInteger assignedById,

    BigInteger releasedById,

    Boolean isPrimary
) {
}
