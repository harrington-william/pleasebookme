package com.pleasebookme.server.resource.resourceservice.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record ResourceServiceRequest(
    @NotNull
    BigInteger resourceId,

    @NotNull
    BigInteger serviceId
) {
}
