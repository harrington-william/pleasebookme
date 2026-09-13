import { CalendarDays, Clock, MapPin, Pencil, UserRound, X } from "lucide-react";
import Link from "next/link";
import type { ReactNode } from "react";

import { BookingStatusBadge } from "@/features/bookings/components/booking-status-badge";
import { CancelBookingButton } from "@/features/bookings/components/cancel-booking-button";
import { buildBookingSearchParams } from "@/features/bookings/schemas/booking-query";
import type {
  Attendee,
  BookingListEntry,
  BookingQuery,
} from "@/features/bookings/types/booking";

function displayReference(bookingUid: string): string {
  // Derived display reference only; the platform does not issue BK identifiers.
  return `BK-${bookingUid.replaceAll("-", "").slice(0, 8).toUpperCase()}`;
}

function safeTimezone(timezone: string): string {
  try {
    new Intl.DateTimeFormat("en-US", { timeZone: timezone }).format(new Date());
    return timezone;
  } catch {
    return "UTC";
  }
}

function formatDate(value: string, timezone: string): string {
  return new Intl.DateTimeFormat("en-US", {
    dateStyle: "medium",
    timeZone: timezone,
  }).format(new Date(value));
}

function formatTimeRange(start: string, end: string, timezone: string): string {
  const formatter = new Intl.DateTimeFormat("en-US", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
    timeZone: timezone,
  });

  return `${formatter.format(new Date(start))} - ${formatter.format(
    new Date(end)
  )} ${timezone}`;
}

function initialsFor(name: string): string {
  const parts = name
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2);

  if (parts.length === 0) return "?";

  return parts.map((part) => part.charAt(0).toUpperCase()).join("");
}

function DetailItem({
  icon,
  label,
  value,
}: {
  icon: ReactNode;
  label: string;
  value: string;
}) {
  return (
    <div className="flex gap-sm">
      <span className="mt-0.5 text-muted-foreground">{icon}</span>
      <div>
        <p className="text-label-md tracking-wider text-muted-foreground uppercase">
          {label}
        </p>
        <p className="text-body-md text-foreground">{value}</p>
      </div>
    </div>
  );
}

function AttendeeBlock({ attendee }: { attendee: Attendee }) {
  return (
    <div className="rounded-lg border border-border bg-surface-container p-md">
      <div className="mb-md flex items-center gap-sm">
        <span className="flex size-10 shrink-0 items-center justify-center rounded-lg border border-border bg-background font-mono text-mono-label text-primary">
          {initialsFor(attendee.name)}
        </span>
        <div className="min-w-0">
          <p className="truncate text-body-md font-medium text-foreground">
            {attendee.name}
          </p>
          <p className="truncate font-mono text-mono-label text-muted-foreground">
            {attendee.email ?? "No email"}
          </p>
        </div>
      </div>

      <dl className="grid grid-cols-1 gap-sm text-body-md sm:grid-cols-2">
        <div>
          <dt className="text-label-md tracking-wider text-muted-foreground uppercase">
            Phone
          </dt>
          <dd className="text-foreground">{attendee.phone}</dd>
        </div>
        <div>
          <dt className="text-label-md tracking-wider text-muted-foreground uppercase">
            Timezone
          </dt>
          <dd className="text-foreground">{attendee.timezone ?? "—"}</dd>
        </div>
      </dl>

      {attendee.noShow ? (
        <p className="mt-sm rounded-sm border border-warning/30 bg-warning/10 px-xs py-base text-label-md text-warning">
          No-show recorded
        </p>
      ) : null}
    </div>
  );
}

export function BookingDetailPanel({
  entry,
  query,
  organizationTimezone,
}: {
  entry: BookingListEntry;
  query: BookingQuery;
  organizationTimezone: string;
}) {
  const timezone = safeTimezone(organizationTimezone);
  const closeHref = buildBookingSearchParams(query, { booking: undefined });

  return (
    <aside className="fixed inset-y-0 right-0 z-30 flex w-full max-w-[420px] flex-col border-l border-border bg-surface shadow-[0_20px_40px_rgba(0,0,0,0.4)] md:sticky md:top-0 md:h-[calc(100vh-96px)]">
      <div className="flex items-start justify-between gap-md border-b border-border p-lg">
        <div className="min-w-0">
          <div className="mb-xs flex flex-wrap items-center gap-sm">
            <span className="font-mono text-mono-label text-muted-foreground">
              {displayReference(entry.booking.bookingUid)}
            </span>
            <BookingStatusBadge status={entry.booking.status} />
          </div>
          <h2 className="text-headline-md text-foreground">
            {entry.booking.title}
          </h2>
        </div>
        <Link
          href={closeHref || "?"}
          aria-label="Close booking details"
          className="flex size-8 shrink-0 items-center justify-center rounded-lg text-muted-foreground transition-colors hover:bg-surface-hover hover:text-foreground"
        >
          <X className="size-4" aria-hidden="true" />
        </Link>
      </div>

      <div className="flex-1 space-y-xl overflow-y-auto p-lg">
        <div className="grid grid-cols-2 gap-sm">
          {/* Reserved: this task has no edit endpoint or edit page. */}
          <button
            type="button"
            disabled
            title="Editing bookings is not part of this phase."
            className="inline-flex h-9 cursor-not-allowed items-center justify-center gap-xs rounded-lg border border-border bg-surface-container px-md text-label-md text-muted-foreground opacity-50"
          >
            <Pencil className="size-4" aria-hidden="true" />
            Edit
          </button>
          <CancelBookingButton
            bookingId={entry.booking.bookingId}
            title={entry.booking.title}
            disabled={entry.booking.status === "CANCELLED"}
          />
        </div>

        <section className="space-y-md">
          <h3 className="text-label-md tracking-wider text-muted-foreground uppercase">
            Booking Details
          </h3>
          <div className="grid grid-cols-1 gap-md sm:grid-cols-2">
            <DetailItem
              icon={<CalendarDays className="size-4" aria-hidden="true" />}
              label="Date"
              value={formatDate(entry.booking.startTime, timezone)}
            />
            <DetailItem
              icon={<Clock className="size-4" aria-hidden="true" />}
              label="Time"
              value={formatTimeRange(
                entry.booking.startTime,
                entry.booking.endTime,
                timezone
              )}
            />
            <DetailItem
              icon={<UserRound className="size-4" aria-hidden="true" />}
              label="Staff"
              value={entry.primaryResource?.name ?? "—"}
            />
            <DetailItem
              icon={<MapPin className="size-4" aria-hidden="true" />}
              label="Location"
              value={entry.booking.location ?? "—"}
            />
          </div>
        </section>

        <section className="space-y-md">
          <h3 className="text-label-md tracking-wider text-muted-foreground uppercase">
            Attendee
          </h3>
          {entry.attendees.length > 0 ? (
            entry.attendees.map((attendee) => (
              <AttendeeBlock key={attendee.attendeeId} attendee={attendee} />
            ))
          ) : (
            <p className="rounded-lg border border-dashed border-border px-md py-sm text-body-md text-muted-foreground">
              —
            </p>
          )}
        </section>
      </div>
    </aside>
  );
}
