import { Download } from "lucide-react";
import type { Metadata } from "next";
import { redirect } from "next/navigation";

import { BookingDetailPanel } from "@/features/bookings/components/booking-detail-panel";
import { BookingFilters } from "@/features/bookings/components/booking-filters";
import { BookingPagination } from "@/features/bookings/components/booking-pagination";
import { BookingTable } from "@/features/bookings/components/booking-table";
import { BookingTabs } from "@/features/bookings/components/booking-tabs";
import {
  hasActiveBookingFilters,
  parseBookingQuery,
  type BookingSearchParams,
} from "@/features/bookings/schemas/booking-query";
import { listMyBookingsOnPlatform } from "@/features/bookings/services/booking-gateway";
import type {
  BookingListEntry,
  BookingListResult,
} from "@/features/bookings/types/booking";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Bookings",
  description: "Manage reservations and review booking details.",
};

function selectedEntry(
  entries: BookingListEntry[],
  bookingId: number | undefined
): BookingListEntry | null {
  if (!bookingId) return null;

  return entries.find((entry) => entry.booking.bookingId === bookingId) ?? null;
}

export default async function BookingsPage({
  searchParams,
}: {
  searchParams: Promise<BookingSearchParams>;
}) {
  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  const query = parseBookingQuery(await searchParams);

  let result: BookingListResult | null = null;
  let loadError: string | null = null;

  try {
    result = await withAccessToken(
      (accessToken) =>
        listMyBookingsOnPlatform(accessToken, actor.subject, query),
      { allowSessionWrite: false }
    );
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect(SESSION_EXPIRED_REDIRECT);
    }

    loadError = "Could not load your bookings. Please refresh to try again.";
  }

  const activePanel = result
    ? selectedEntry(result.entries, query.booking)
    : null;

  return (
    <main className="mx-auto flex w-full max-w-7xl flex-col gap-lg p-md md:p-2xl">
      <div className="flex flex-col justify-between gap-md sm:flex-row sm:items-end">
        <div className="space-y-xs">
          <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
            Bookings
          </h1>
          <p className="text-body-md text-muted-foreground">
            Manage reservations and review the attendee snapshot for each
            booking.
          </p>
        </div>

        {/* Reserved: the platform has no bookings export endpoint. */}
        <button
          type="button"
          disabled
          title="Exporting bookings is not backed by a platform endpoint yet."
          className="inline-flex h-9 cursor-not-allowed items-center justify-center gap-xs rounded-lg border border-border px-md text-label-md text-muted-foreground opacity-50"
        >
          <Download className="size-4" aria-hidden="true" />
          Export
        </button>
      </div>

      {loadError || !result ? (
        <p className="text-body-md text-destructive">{loadError}</p>
      ) : (
        <div className="flex gap-lg">
          <div className="flex min-w-0 flex-1 flex-col gap-lg">
            <BookingTabs query={query} />
            <BookingFilters
              query={query}
              services={result.services}
              resources={result.resources}
            />
            <BookingTable
              entries={result.entries}
              query={query}
              filtered={
                hasActiveBookingFilters(query) || query.tab !== "UPCOMING"
              }
            />
            <BookingPagination query={query} page={result.page} />
          </div>

          {activePanel ? (
            <BookingDetailPanel
              entry={activePanel}
              query={query}
              organizationTimezone={result.organizationTimezone}
            />
          ) : null}
        </div>
      )}
    </main>
  );
}
