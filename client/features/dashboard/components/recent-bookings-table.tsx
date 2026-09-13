import { BookingStatusBadge } from "@/features/bookings/components/booking-status-badge";
import type { DashboardBookingRow } from "@/features/dashboard/types/dashboard";

function displayReference(bookingUid: string): string {
  // Derived display reference only; the platform does not issue BK identifiers.
  return `BK-${bookingUid.replaceAll("-", "").slice(0, 8).toUpperCase()}`;
}

function formatStartTime(value: string, timezone: string): string {
  const formatTimezone = timezone === "Z" ? "UTC" : timezone;

  return new Intl.DateTimeFormat("en-GB", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
    timeZone: formatTimezone,
  }).format(new Date(value));
}

export function RecentBookingsTable({
  bookings,
  timezone,
}: {
  bookings: DashboardBookingRow[];
  timezone: string;
}) {
  if (bookings.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-border px-md py-2xl text-center">
        <p className="text-body-md text-muted-foreground">No bookings yet.</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-border bg-surface">
      <table
        className="w-full min-w-5xl border-collapse text-left"
        aria-label="Recent bookings"
      >
        <thead>
          <tr className="border-b border-border text-label-md tracking-wider text-muted-foreground uppercase">
            <th className="px-md py-sm font-medium">Booking</th>
            <th className="px-md py-sm font-medium">Customer</th>
            <th className="px-md py-sm font-medium">Service</th>
            <th className="px-md py-sm font-medium">Resource</th>
            <th className="px-md py-sm font-medium">Status</th>
            <th className="px-md py-sm font-medium">Start</th>
          </tr>
        </thead>
        <tbody>
          {bookings.map((booking) => (
            <tr
              key={booking.bookingId}
              className="border-b border-border last:border-b-0"
            >
              <td className="px-md py-sm font-mono text-mono-label text-primary">
                {displayReference(booking.bookingUid)}
              </td>
              <td className="px-md py-sm text-body-md text-foreground">
                {booking.customerName ?? "—"}
              </td>
              <td className="px-md py-sm text-body-md text-foreground">
                {booking.serviceTitle ?? "—"}
              </td>
              <td className="px-md py-sm text-body-md text-muted-foreground">
                {booking.resourceName ?? "—"}
              </td>
              <td className="px-md py-sm">
                <BookingStatusBadge status={booking.status} />
              </td>
              <td className="whitespace-nowrap px-md py-sm text-body-md text-muted-foreground">
                {formatStartTime(booking.startTime, timezone)}
                <span className="ml-xs font-mono text-mono-label">
                  {timezone}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
