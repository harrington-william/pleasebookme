package com.pleasebookme.server.widget.widgets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Accepts only the fixed-format credential pairs minted by the platform. */
public record WidgetCredentialRequest(
    @NotBlank
    @Pattern(regexp = "^pbm_pk_[A-Za-z0-9_-]{22}$")
    String publicKey,

    @NotBlank
    @Pattern(regexp = "^pbm_sk_[A-Za-z0-9_-]{43}$")
    String secretKey
) {
}
