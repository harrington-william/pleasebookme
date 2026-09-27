package com.pleasebookme.server.service.widget.barbershop.dto;

import com.pleasebookme.server.global.enums.Currency;
import com.pleasebookme.server.global.enums.WeekStart;

import java.math.BigDecimal;
import java.util.List;

public record WidgetServiceResponse(
    Organization organization,
    String slug,
    String title,
    String description,
    String location,
    Integer durationMinutes,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Currency currency,
    String scheduleTimezone,
    Integer minimumNotice,
    Integer maximumAdvanceBooking,
    Boolean autoConfirm,
    String successRedirectUrl,
    List<Integer> availableWeekdays
) {
    public record Organization(
        String name,
        String slug,
        String logoUrl,
        String timezone,
        WeekStart weekStart
    ) {}
}
