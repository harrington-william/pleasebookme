export type WeekStart = "SUNDAY" | "MONDAY";
export type Currency = "USD" | "AUD" | "SGD" | "GBP" | "VND";
export type BookingStatus = "ACCEPTED" | "AWAITING_HOST";

export interface ServiceSummary {
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

export interface Organization {
  name: string;
  slug: string;
  logoUrl: string | null;
  bannerUrl: string | null;
  bio: string | null;
  timezone: string;
  weekStart: WeekStart;
  ecosystem: string;
  services: ServiceSummary[];
}

export interface Service extends ServiceSummary {
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

export interface BookingRequest {
  name: string;
  phone: string;
  email?: string;
  timezone?: string;
  slotStart: string;
  notes?: string;
}

export interface Booking {
  bookingUid: string;
  status: BookingStatus;
  startTime: string;
  endTime: string;
  timezone: string;
  serviceTitle: string;
  organizationName: string;
}

export interface BookingApi {
  getOrganization(): Promise<Organization>;
  getService(serviceSlug: string): Promise<Service>;
  getSlots(serviceSlug: string, date: string): Promise<AvailableSlots>;
  createBooking(serviceSlug: string, request: BookingRequest): Promise<Booking>;
}
