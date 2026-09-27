export type WeekStart = "SUNDAY" | "MONDAY";
export type Currency = "USD" | "AUD" | "SGD" | "GBP" | "VND";
export type PublicBookingStatus = "ACCEPTED" | "AWAITING_HOST";

export interface PublicServiceSummary {
  slug: string;
  title: string;
  description: string | null;
  location: string | null;
  durationMinutes: number;
  minPrice: number | null;
  maxPrice: number | null;
  currency: Currency;
  autoConfirm: boolean;
}

export interface PublicOrganization {
  name: string;
  slug: string;
  logoUrl: string | null;
  bannerUrl: string | null;
  bio: string | null;
  timezone: string;
  weekStart: WeekStart;
  services: PublicServiceSummary[];
}

export interface PublicService extends PublicServiceSummary {
  organization: {
    name: string;
    slug: string;
    logoUrl: string | null;
    timezone: string;
    weekStart: WeekStart;
  };
  scheduleTimezone: string;
  minimumNotice: number;
  maximumAdvanceBooking: number;
  successRedirectUrl: string | null;
  availableWeekdays: number[];
}

export interface TimeSlot {
  slotStart: string;
  slotEnd: string;
}

export interface AvailableSlots {
  serviceId: number;
  date: string;
  timezone: string;
  slots: TimeSlot[];
}

export interface PublicBookingRequest {
  name: string;
  phone: string;
  email?: string;
  timezone?: string;
  slotStart: string;
  notes?: string;
}

export interface PublicBooking {
  bookingUid: string;
  status: PublicBookingStatus;
  startTime: string;
  endTime: string;
  timezone: string;
  serviceTitle: string;
  organizationName: string;
}
