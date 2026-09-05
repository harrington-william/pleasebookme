import type { Service } from "@/features/services/types/service";

export const BOOKING_STATUSES = [
  "PENDING",
  "ACCEPTED",
  "REJECTED",
  "AWAITING_HOST",
  "CANCELLED",
] as const;

export type BookingStatus = (typeof BOOKING_STATUSES)[number];

export const BOOKING_TABS = [
  "UPCOMING",
  "PENDING",
  "PAST",
  "CANCELLED",
  "ALL",
] as const;

export type BookingTab = (typeof BOOKING_TABS)[number];

export const BOOKING_SORT_FIELDS = [
  "startTime",
  "endTime",
  "title",
  "status",
  "createdAt",
  "updatedAt",
] as const;

export type BookingSortField = (typeof BOOKING_SORT_FIELDS)[number];

export interface Booking {
  bookingId: number;
  bookingUid: string;
  idempotencyKey: string | null;
  userId: number;
  title: string;
  description: string | null;
  startTime: string;
  endTime: string;
  serviceId: number;
  location: string | null;
  status: BookingStatus;
  paid: boolean;
  cancelledById: number | null;
  cancellationReason: string | null;
  rejectionReason: string | null;
  rescheduled: boolean;
  rescheduledById: number | null;
  noShowHost: boolean;
  deletedAt: string | null;
  deletedById: number | null;
  destinationCalendarId: number | null;
  destinationSheetsId: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface BookingPage {
  content: Booking[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Attendee {
  attendeeId: number;
  bookingId: number;
  email: string | null;
  phone: string;
  name: string;
  locale: "en" | "vi" | null;
  timezone: string | null;
  noShow: boolean;
  createdAt: string;
}

export interface BookingResourceLink {
  bookingId: number;
  resourceId: number;
  isPrimary: boolean;
  assignedAt: string;
}

export interface ResourceLookup {
  resourceId: number;
  name: string;
}

export interface OrganizationSummary {
  organizationId: number;
  timezone: string;
}

export interface BookingQuery {
  page: number;
  size: number;
  tab: BookingTab;
  serviceId?: number;
  resourceId?: number;
  q?: string;
  sort?: string;
  booking?: number;
}

export interface BookingPageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface BookingListEntry {
  booking: Booking;
  service: Service | null;
  primaryResource: ResourceLookup | null;
  attendees: Attendee[];
}

export interface BookingListResult {
  entries: BookingListEntry[];
  page: BookingPageMeta;
  services: Service[];
  resources: ResourceLookup[];
  organizationTimezone: string;
}
