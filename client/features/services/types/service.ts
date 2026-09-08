export const CURRENCIES = ["USD", "AUD", "SGD", "GBP", "VND"] as const;
export type Currency = (typeof CURRENCIES)[number];

/**
 * core.booking_policies.booking_window_type is a plain VARCHAR(50) with no CHECK
 * constraint, no native enum and no server-side consumer yet — the database would
 * accept any string. These two are the vocabulary this client writes.
 */
export const BOOKING_WINDOW_TYPES = ["ROLLING", "FIXED"] as const;
export type BookingWindowType = (typeof BOOKING_WINDOW_TYPES)[number];

export interface Service {
  serviceId: number;
  title: string;
  slug: string;
  description: string | null;
  interfaceLanguage: "en" | "vi" | null;
  location: string | null;
  userId: number;
  profileId: number;
  organizationId: number;
  scheduleId: number;
  periodType: string;
  timezone: string;
  minPrice: number | null;
  maxPrice: number | null;
  currency: Currency;
  requiresConfirmation: boolean;
  disableCancelling: boolean;
  disableRescheduling: boolean;
  successRedirectUrl: string | null;
  isInstantService: boolean;
  maxActiveBookingPerBooker: number | null;
  destinationCalendarId: number | null;
  destinationSheetsId: number | null;
  bookingPolicy?: BookingPolicy | null;
  createdAt: string;
  updatedAt: string;
}

// POST /api/v1/services, PUT /api/v1/services/{serviceId}
export interface ServiceRequest {
  title: string;
  slug: string;
  description?: string | null;
  location?: string | null;
  scheduleId: number;
  timezone?: string;
  minPrice?: number | null;
  maxPrice?: number | null;
  currency?: Currency;
  requiresConfirmation?: boolean;
  disableCancelling?: boolean;
  successRedirectUrl?: string | null;
  maxActiveBookingPerBooker?: number | null;
  bookingPolicy?: ServiceBookingPolicyRequest | null;
}

export interface BookingPolicy {
  bookingPolicyId: number;
  serviceId: number;
  bookingMode: "FIXED" | "FLEXIBLE" | "HYBRID";
  defaultDuration: number;
  minimumDuration: number | null;
  maximumDuration: number | null;
  /** Minutes. The column is a bare INTEGER — see toMinutes/fromMinutes. */
  minimumNotice: number;
  /** Minutes. The column is a bare INTEGER — see toMinutes/fromMinutes. */
  maximumAdvanceBooking: number;
  slotInterval: number;
  beforeBuffer: number;
  afterBuffer: number;
  allowOverlap: boolean;
  allowMultipleAttendee: boolean;
  requiresPayment: boolean;
  autoConfirm: boolean;
  bookingWindowType: string;
  capacity: number;
  createdAt: string;
  updatedAt: string;
}

// Nested inside ServiceRequest; the platform upserts it with the service.
export interface ServiceBookingPolicyRequest {
  bookingMode?: "FIXED" | "FLEXIBLE" | "HYBRID";
  defaultDuration?: number;
  minimumDuration?: number | null;
  maximumDuration?: number | null;
  minimumNotice: number;
  maximumAdvanceBooking: number;
  slotInterval?: number;
  beforeBuffer?: number;
  afterBuffer?: number;
  allowOverlap?: boolean;
  allowMultipleAttendee?: boolean;
  requiresPayment?: boolean;
  autoConfirm?: boolean;
  bookingWindowType: string;
  capacity: number;
}

// POST /api/v1/booking-policies, PUT /api/v1/booking-policies/{bookingPolicyId}
export interface BookingPolicyRequest extends ServiceBookingPolicyRequest {
  serviceId: number;
}

export interface ServiceCatalogEntry {
  service: Service;
  bookingPolicy: BookingPolicy | null;
}
