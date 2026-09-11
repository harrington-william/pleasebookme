package com.pleasebookme.server.service.dashboard.dto;

/**
 * Operational booking counts for the dashboard.
 *
 * <p>Revenue is deliberately absent because bookings carry no charged amount or
 * currency, and the billing schema is outside PLATFORM V1.0.0.</p>
 *
 * @param todaysBookings live bookings starting on the organization's local date
 * @param pending pending work across all dates
 * @param cancellations cancelled or rejected bookings starting on the local date
 */
public record DashboardMetricsResponse(
    long todaysBookings,
    long pending,
    long cancellations
) {}
