package com.pleasebookme.server.customer.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record CustomerTagRequest(
    @NotNull
    BigInteger customerId,

    @NotBlank
    @Size(max = 50)
    String tag
) {
}
