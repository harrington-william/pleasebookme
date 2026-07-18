package com.pleasebookme.server.customer.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record CustomerNoteRequest(
    @NotNull
    BigInteger customerId,

    BigInteger authorUserId,

    @NotBlank
    String content
) {
}
