package com.pleasebookme.server.core.attendee.dto;

import com.pleasebookme.server.global.enums.Locale;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigInteger;

public record AttendeeRequest(
    @NotNull
    BigInteger bookingId,

    @Size(max = 255)
    String email,

    @NotBlank
    @Size(max = 50)
    String phone,

    @NotBlank
    @Size(max = 255)
    String name,

    Locale locale,

    @Size(max = 100)
    String timezone,

    Boolean noShow
) {
}
