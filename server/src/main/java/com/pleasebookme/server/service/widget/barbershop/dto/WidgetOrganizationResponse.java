package com.pleasebookme.server.service.widget.barbershop.dto;

import com.pleasebookme.server.global.enums.WeekStart;

import java.util.List;

public record WidgetOrganizationResponse(
    String name,
    String slug,
    String logoUrl,
    String bannerUrl,
    String bio,
    String timezone,
    WeekStart weekStart,
    String ecosystem,
    List<WidgetServiceSummary> services
) {}
