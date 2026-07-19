package com.pleasebookme.server.core.service.dto;

import com.pleasebookme.server.global.enums.Currency;
import com.pleasebookme.server.global.enums.Locale;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.BigInteger;

public record ServiceRequest(
    @NotBlank
    @Size(max = 255)
    String title,

    @NotBlank
    @Size(max = 255)
    String slug,

    String description,

    Locale interfaceLanguage,

    String location,

    @NotNull
    BigInteger userId,

    @NotNull
    BigInteger profileId,

    @NotNull
    BigInteger organizationId,

    @NotNull
    BigInteger scheduleId,

    @Size(max = 50)
    String periodType,

    @Size(max = 100)
    String timezone,

    BigDecimal minPrice,

    BigDecimal maxPrice,

    Currency currency,

    Boolean requiresConfirmation,

    Boolean disableCancelling,

    Boolean disableRescheduling,

    String successRedirectUrl,

    Boolean isInstantService,

    Integer maxActiveBookingPerBooker,

    BigInteger destinationCalendarId,

    BigInteger destinationSheetsId
) {
}
