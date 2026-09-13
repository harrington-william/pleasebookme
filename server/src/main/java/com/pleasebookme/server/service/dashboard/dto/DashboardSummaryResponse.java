package com.pleasebookme.server.service.dashboard.dto;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

public record DashboardSummaryResponse(
    BigInteger organizationId,
    String timezone,
    LocalDate date,
    DashboardMetricsResponse metrics,
    List<DashboardBookingRowResponse> recentBookings
) {}
