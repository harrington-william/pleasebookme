import Link from "next/link";

import {
  buildBookingSearchParams,
  DEFAULT_BOOKING_TAB,
} from "@/features/bookings/schemas/booking-query";
import type { BookingQuery, BookingTab } from "@/features/bookings/types/booking";
import { cn } from "@/lib/utils";

const TABS: { value: BookingTab; label: string }[] = [
  { value: "UPCOMING", label: "Upcoming" },
  { value: "PENDING", label: "Pending" },
  { value: "PAST", label: "Past" },
  { value: "CANCELLED", label: "Cancelled" },
  { value: "ALL", label: "All" },
];

export function BookingTabs({ query }: { query: BookingQuery }) {
  return (
    <nav
      aria-label="Booking views"
      className="flex gap-lg border-b border-border"
    >
      {TABS.map((tab) => {
        const active = query.tab === tab.value;
        const href = buildBookingSearchParams(query, {
          tab: tab.value,
          page: 0,
        });

        return (
          <Link
            key={tab.value}
            href={href || "?"}
            aria-current={active ? "page" : undefined}
            className={cn(
              "border-b-2 px-base pb-sm text-label-md transition-colors",
              active
                ? "border-primary text-foreground"
                : "border-transparent text-muted-foreground hover:text-foreground",
              tab.value === DEFAULT_BOOKING_TAB && active && "font-medium"
            )}
          >
            {tab.label}
          </Link>
        );
      })}
    </nav>
  );
}
