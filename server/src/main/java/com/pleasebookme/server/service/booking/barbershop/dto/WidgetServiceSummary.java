package com.pleasebookme.server.service.booking.barbershop.dto;

import com.pleasebookme.server.global.enums.Currency;

import java.math.BigDecimal;

public record WidgetServiceSummary(
    String slug,
    String title,
    String description,
    String location,
    Integer durationMinutes,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Currency currency,
    Boolean autoConfirm
) {}
