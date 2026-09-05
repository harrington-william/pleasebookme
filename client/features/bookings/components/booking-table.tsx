import Link from "next/link";

import { BookingStatusBadge } from "@/features/bookings/components/booking-status-badge";
import { buildBookingSearchParams } from "@/features/bookings/schemas/booking-query";
import type {
  Attendee,
  BookingListEntry,
  BookingQuery,
} from "@/features/bookings/types/booking";
import { cn } from "@/lib/utils";

function initialsFor(name: string): string {
  const parts = name
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2);

  if (parts.length === 0) return "?";

  return parts.map((part) => part.charAt(0).toUpperCase()).join("");
}

function primaryAttendee(attendees: Attendee[]): Attendee | null {
  return [...attendees].sort(
    (first, second) => first.attendeeId - second.attendeeId
  )[0] ?? null;
}

export function BookingTable({
  entries,
  query,
  filtered = false,
}: {
  entries: BookingListEntry[];
  query: BookingQuery;
  filtered?: boolean;
}) {
  if (entries.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-border px-md py-2xl text-center">
        <p className="text-body-md text-muted-foreground">
          {filtered
            ? "No bookings match this view. Try changing the filters."
            : "No bookings yet."}
        </p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-border bg-surface">
      <table className="w-full min-w-3xl border-collapse text-left">
        <thead>
          <tr className="border-b border-border text-label-md tracking-wider text-muted-foreground uppercase">
            <th className="px-md py-sm font-medium">Customer</th>
            <th className="px-md py-sm font-medium">Service</th>
            <th className="px-md py-sm font-medium">Staff</th>
            <th className="px-md py-sm font-medium">Status</th>
          </tr>
        </thead>
        <tbody>
          {entries.map((entry) => {
            const attendee = primaryAttendee(entry.attendees);
            const selected = query.booking === entry.booking.bookingId;
            const href = buildBookingSearchParams(query, {
              booking: entry.booking.bookingId,
            });

            return (
              <tr
                key={entry.booking.bookingId}
                className={cn(
                  "border-b border-border transition-colors last:border-b-0 hover:bg-surface-hover",
                  selected && "bg-surface-hover/70"
                )}
              >
                <td className="px-md py-sm">
                  <Link
                    href={href}
                    className="flex min-w-[220px] items-center gap-sm"
                  >
                    {attendee ? (
                      <>
                        <span className="flex size-8 shrink-0 items-center justify-center rounded-lg border border-border bg-surface-container font-mono text-mono-label text-primary">
                          {initialsFor(attendee.name)}
                        </span>
                        <span className="min-w-0">
                          <span className="block truncate text-body-md font-medium text-foreground">
                            {attendee.name}
                            {entry.attendees.length > 1
                              ? ` +${entry.attendees.length - 1}`
                              : ""}
                          </span>
                          <span className="block truncate font-mono text-mono-label text-muted-foreground">
                            {attendee.email ?? "No email"}
                          </span>
                        </span>
                      </>
                    ) : (
                      <span className="text-body-md text-muted-foreground">
                        —
                      </span>
                    )}
                  </Link>
                </td>
                <td className="px-md py-sm">
                  <Link
                    href={href}
                    className="block min-w-[220px] truncate text-body-md text-foreground"
                  >
                    {entry.service?.title ?? "—"}
                  </Link>
                </td>
                <td className="px-md py-sm">
                  <Link
                    href={href}
                    className="block min-w-[140px] truncate text-body-md text-muted-foreground"
                  >
                    {entry.primaryResource?.name ?? "—"}
                  </Link>
                </td>
                <td className="px-md py-sm">
                  <Link href={href} className="inline-flex">
                    <BookingStatusBadge status={entry.booking.status} />
                  </Link>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
