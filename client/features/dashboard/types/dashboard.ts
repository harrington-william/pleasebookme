import type { BookingStatus } from "@/features/bookings/types/booking";

export interface DashboardMetrics {
  todaysBookings: number;
  pending: number;
  cancellations: number;
}

export interface DashboardBookingRow {
  bookingId: number;
  bookingUid: string;
  title: string;
  startTime: string;
  endTime: string;
  status: BookingStatus;
  serviceId: number;
  serviceTitle: string;
  customerName: string | null;
  resourceName: string | null;
}

export interface DashboardSummary {
  organizationId: number;
  timezone: string;
  date: string;
  metrics: DashboardMetrics;
  recentBookings: DashboardBookingRow[];
}
