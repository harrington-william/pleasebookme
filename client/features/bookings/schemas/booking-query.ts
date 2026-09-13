import {
  BOOKING_SORT_FIELDS,
  BOOKING_TABS,
  type BookingQuery,
  type BookingSortField,
  type BookingTab,
} from "@/features/bookings/types/booking";

export const DEFAULT_BOOKING_PAGE_SIZE = 20;
export const DEFAULT_BOOKING_TAB: BookingTab = "UPCOMING";

export type BookingSearchParams = Record<string, string | string[] | undefined>;

function first(value: string | string[] | undefined): string | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  const trimmed = raw?.trim();
  return trimmed ? trimmed : undefined;
}

function parseTab(value: string | undefined): BookingTab {
  return BOOKING_TABS.includes(value as BookingTab)
    ? (value as BookingTab)
    : DEFAULT_BOOKING_TAB;
}

function parseSort(value: string | undefined): string | undefined {
  if (!value) return undefined;

  const [field, direction] = value.split(",");
  if (!BOOKING_SORT_FIELDS.includes(field as BookingSortField)) {
    return undefined;
  }

  return direction === "asc" || direction === "desc"
    ? `${field},${direction}`
    : field;
}

function parsePositiveInteger(value: string | undefined): number | undefined {
  if (!value) return undefined;

  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
}

export function parseBookingQuery(
  searchParams: BookingSearchParams
): BookingQuery {
  const page = Number(first(searchParams.page));

  return {
    page: Number.isInteger(page) && page >= 0 ? page : 0,
    size: DEFAULT_BOOKING_PAGE_SIZE,
    tab: parseTab(first(searchParams.tab)),
    serviceId: parsePositiveInteger(first(searchParams.serviceId)),
    resourceId: parsePositiveInteger(first(searchParams.resourceId)),
    q: first(searchParams.q),
    sort: parseSort(first(searchParams.sort)),
    booking: parsePositiveInteger(first(searchParams.booking)),
  };
}

export function buildBookingSearchParams(
  query: BookingQuery,
  overrides: Partial<BookingQuery> = {}
): string {
  const merged = { ...query, ...overrides };
  const params = new URLSearchParams();

  if (merged.q) params.set("q", merged.q);
  if (merged.serviceId) params.set("serviceId", String(merged.serviceId));
  if (merged.resourceId) params.set("resourceId", String(merged.resourceId));
  if (merged.sort) params.set("sort", merged.sort);
  if (merged.tab !== DEFAULT_BOOKING_TAB) params.set("tab", merged.tab);
  if (merged.booking) params.set("booking", String(merged.booking));
  if (merged.page > 0) params.set("page", String(merged.page));

  const serialized = params.toString();
  return serialized ? `?${serialized}` : "";
}

export function hasActiveBookingFilters(query: BookingQuery): boolean {
  return Boolean(query.q || query.serviceId || query.resourceId);
}
