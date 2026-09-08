import { z } from "zod";

import {
  DURATION_UNITS,
  fromMinutes,
  toMinutes,
  type DurationUnit,
} from "@/features/services/schemas/duration-unit";
import {
  BOOKING_WINDOW_TYPES,
  CURRENCIES,
  type BookingWindowType,
  type ServiceCatalogEntry,
} from "@/features/services/types/service";

const TITLE_MAX = 255;
/**
 * Placeholder only — never a default value. core.services.success_redirect_url is
 * nullable, so leaving the field blank has to persist as null rather than quietly
 * pointing every new service at this host.
 */
export const SUCCESS_REDIRECT_URL_PLACEHOLDER = "https://pleasebookmee.com";

function nonNegativeInteger(message: string) {
  return z
    .string()
    .trim()
    .min(1, message)
    .refine(
      (value) => Number.isInteger(Number(value)) && Number(value) >= 0,
      message
    );
}

function positiveInteger(message: string) {
  return z
    .string()
    .trim()
    .min(1, message)
    .refine(
      (value) => Number.isInteger(Number(value)) && Number(value) >= 1,
      message
    );
}

export const serviceFormSchema = z.object({
  // core.services
  title: z
    .string()
    .trim()
    .min(1, "Title is required.")
    .max(TITLE_MAX, `Must be at most ${TITLE_MAX} characters.`),
  slug: z.string().trim().min(1).max(TITLE_MAX),
  description: z.string().trim().max(2000).optional(),
  location: z.string().trim().max(2000).optional(),
  scheduleId: z
    .string()
    .min(1, "Select an availability ruleset for this service."),
  timezone: z.string().min(1, "Select a timezone."),
  price: z
    .string()
    .trim()
    .optional()
    .refine(
      (value) => !value || (!Number.isNaN(Number(value)) && Number(value) >= 0),
      "Enter a valid, non-negative price."
    ),
  currency: z.enum(CURRENCIES),
  successRedirectUrl: z
    .string()
    .trim()
    .optional()
    .refine(
      (value) => !value || z.url().safeParse(value).success,
      `Enter a valid URL, for example ${SUCCESS_REDIRECT_URL_PLACEHOLDER}`
    ),
  maxActiveBookingPerBooker: z
    .string()
    .trim()
    .optional()
    .refine(
      (value) =>
        !value || (Number.isInteger(Number(value)) && Number(value) >= 1),
      "Enter a whole number of 1 or more, or leave blank for no limit."
    ),
  requiresConfirmation: z.boolean(),
  disableCancelling: z.boolean(),

  // core.booking_policies
  defaultDuration: positiveInteger("Duration must be at least 1 minute."),
  beforeBuffer: nonNegativeInteger("Buffer cannot be negative."),
  afterBuffer: nonNegativeInteger("Buffer cannot be negative."),
  minimumNoticeAmount: nonNegativeInteger("Minimum notice cannot be negative."),
  minimumNoticeUnit: z.enum(DURATION_UNITS),
  maximumAdvanceAmount: positiveInteger(
    "Maximum advance booking must be at least 1."
  ),
  maximumAdvanceUnit: z.enum(DURATION_UNITS),
  capacity: positiveInteger("Capacity must be at least 1."),
  bookingWindowType: z.enum(BOOKING_WINDOW_TYPES),
});

export type ServiceFormValues = z.infer<typeof serviceFormSchema>;

export const DEFAULT_SERVICE_FORM_VALUES: ServiceFormValues = {
  title: "",
  slug: "",
  description: "",
  location: "",
  scheduleId: "",
  timezone: "Australia/Sydney",
  price: "",
  currency: "USD",
  successRedirectUrl: "",
  maxActiveBookingPerBooker: "",
  requiresConfirmation: false,
  disableCancelling: false,
  defaultDuration: "30",
  beforeBuffer: "0",
  afterBuffer: "0",
  minimumNoticeAmount: "2",
  minimumNoticeUnit: "HOURS",
  maximumAdvanceAmount: "90",
  maximumAdvanceUnit: "DAYS",
  capacity: "1",
  bookingWindowType: "ROLLING",
};

export function slugify(title: string): string {
  return title
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, TITLE_MAX);
}

/**
 * The browser -> BFF contract. Route handlers parse an untrusted body with this,
 * so it is a real schema rather than a bare interface; durations have already been
 * normalized to minutes by the time a payload reaches it.
 */
export const servicePayloadSchema = z.object({
  title: z.string().trim().min(1).max(TITLE_MAX),
  slug: z.string().trim().min(1).max(TITLE_MAX),
  description: z.string().nullable(),
  location: z.string().nullable(),
  scheduleId: z.number().int().positive(),
  timezone: z.string().min(1),
  price: z.number().nonnegative().nullable(),
  currency: z.enum(CURRENCIES),
  successRedirectUrl: z.string().nullable(),
  maxActiveBookingPerBooker: z.number().int().positive().nullable(),
  requiresConfirmation: z.boolean(),
  disableCancelling: z.boolean(),
  defaultDuration: z.number().int().positive(),
  beforeBuffer: z.number().int().nonnegative(),
  afterBuffer: z.number().int().nonnegative(),
  minimumNotice: z.number().int().nonnegative(),
  maximumAdvanceBooking: z.number().int().positive(),
  capacity: z.number().int().positive(),
  bookingWindowType: z.enum(BOOKING_WINDOW_TYPES),
});

export type ServicePayload = z.infer<typeof servicePayloadSchema>;

function optionalText(value: string | undefined): string | null {
  return value && value.length > 0 ? value : null;
}

function optionalNumber(value: string | undefined): number | null {
  return value && value.length > 0 ? Number(value) : null;
}

export function toServicePayload(values: ServiceFormValues): ServicePayload {
  return {
    title: values.title,
    // Editing keeps the slug it was created with; the form freezes the field and
    // round-trips the stored value, so a retitle can never break a public URL.
    slug: values.slug || slugify(values.title),
    description: optionalText(values.description),
    location: optionalText(values.location),
    scheduleId: Number(values.scheduleId),
    timezone: values.timezone,
    price: optionalNumber(values.price),
    currency: values.currency,
    successRedirectUrl: optionalText(values.successRedirectUrl),
    maxActiveBookingPerBooker: optionalNumber(values.maxActiveBookingPerBooker),
    requiresConfirmation: values.requiresConfirmation,
    disableCancelling: values.disableCancelling,
    defaultDuration: Number(values.defaultDuration),
    beforeBuffer: Number(values.beforeBuffer),
    afterBuffer: Number(values.afterBuffer),
    minimumNotice: toMinutes(
      Number(values.minimumNoticeAmount),
      values.minimumNoticeUnit
    ),
    maximumAdvanceBooking: toMinutes(
      Number(values.maximumAdvanceAmount),
      values.maximumAdvanceUnit
    ),
    capacity: Number(values.capacity),
    bookingWindowType: values.bookingWindowType,
  };
}

function bookingWindowTypeOf(value: string | undefined): BookingWindowType {
  return value === "FIXED" ? "FIXED" : "ROLLING";
}

function durationFieldsOf(
  minutes: number | undefined,
  fallbackAmount: string,
  fallbackUnit: DurationUnit
): { amount: string; unit: DurationUnit } {
  if (minutes === undefined || minutes === null) {
    return { amount: fallbackAmount, unit: fallbackUnit };
  }

  const { amount, unit } = fromMinutes(minutes);
  return { amount: String(amount), unit };
}

export function toEditServiceFormValues(
  entry: ServiceCatalogEntry
): ServiceFormValues {
  const { service, bookingPolicy } = entry;

  const minimumNotice = durationFieldsOf(
    bookingPolicy?.minimumNotice,
    DEFAULT_SERVICE_FORM_VALUES.minimumNoticeAmount,
    DEFAULT_SERVICE_FORM_VALUES.minimumNoticeUnit
  );
  const maximumAdvance = durationFieldsOf(
    bookingPolicy?.maximumAdvanceBooking,
    DEFAULT_SERVICE_FORM_VALUES.maximumAdvanceAmount,
    DEFAULT_SERVICE_FORM_VALUES.maximumAdvanceUnit
  );

  return {
    title: service.title,
    slug: service.slug,
    description: service.description ?? "",
    location: service.location ?? "",
    scheduleId: String(service.scheduleId),
    timezone: service.timezone,
    price: service.minPrice === null ? "" : String(service.minPrice),
    currency: service.currency,
    successRedirectUrl: service.successRedirectUrl ?? "",
    maxActiveBookingPerBooker:
      service.maxActiveBookingPerBooker === null
        ? ""
        : String(service.maxActiveBookingPerBooker),
    // The policy's auto_confirm is the column the client writes; the service's
    // requires_confirmation is only a mirror of it and is being dropped.
    requiresConfirmation:
      bookingPolicy?.autoConfirm ?? service.requiresConfirmation,
    disableCancelling: service.disableCancelling,
    defaultDuration: String(
      bookingPolicy?.defaultDuration ??
        DEFAULT_SERVICE_FORM_VALUES.defaultDuration
    ),
    beforeBuffer: String(bookingPolicy?.beforeBuffer ?? 0),
    afterBuffer: String(bookingPolicy?.afterBuffer ?? 0),
    minimumNoticeAmount: minimumNotice.amount,
    minimumNoticeUnit: minimumNotice.unit,
    maximumAdvanceAmount: maximumAdvance.amount,
    maximumAdvanceUnit: maximumAdvance.unit,
    capacity: String(bookingPolicy?.capacity ?? 1),
    bookingWindowType: bookingWindowTypeOf(bookingPolicy?.bookingWindowType),
  };
}
