import { z } from "zod";

const TITLE_MAX = 255;

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

export const createServiceFormSchema = z.object({
  title: z
    .string()
    .trim()
    .min(1, "Service name is required.")
    .max(TITLE_MAX, `Must be at most ${TITLE_MAX} characters.`),
  description: z.string().trim().max(2000).optional(),
  scheduleId: z
    .string()
    .min(1, "Select an availability ruleset for this service."),
  price: z
    .string()
    .trim()
    .optional()
    .refine(
      (value) => !value || (!Number.isNaN(Number(value)) && Number(value) >= 0),
      "Enter a valid, non-negative price."
    ),

  // core.booking_policies
  defaultDuration: positiveInteger("Duration must be at least 1."),
  beforeBuffer: nonNegativeInteger("Buffer cannot be negative."),
  afterBuffer: nonNegativeInteger("Buffer cannot be negative."),
  minimumNotice: nonNegativeInteger("Minimum notice cannot be negative."),
  maximumAdvanceBooking: positiveInteger(
    "Maximum advance booking must be at least 1."
  ),
  capacity: positiveInteger("Capacity must be at least 1."),
  bookingWindowType: z.enum(["ROLLING", "FIXED"]),
});

export type CreateServiceFormValues = z.infer<typeof createServiceFormSchema>;

export const DEFAULT_CREATE_SERVICE_VALUES: CreateServiceFormValues = {
  title: "",
  description: "",
  scheduleId: "",
  price: "",
  defaultDuration: "60",
  beforeBuffer: "0",
  afterBuffer: "0",
  minimumNotice: "60",
  maximumAdvanceBooking: "90",
  capacity: "1",
  bookingWindowType: "ROLLING",
};

export function slugify(title: string): string {
  return title
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 255);
}

export interface CreateServicePayload {
  title: string;
  slug: string;
  description?: string;
  scheduleId: number;
  price?: number;
  defaultDuration: number;
  beforeBuffer: number;
  afterBuffer: number;
  minimumNotice: number;
  maximumAdvanceBooking: number;
  capacity: number;
  bookingWindowType: "ROLLING" | "FIXED";
}

export function toCreateServicePayload(
  values: CreateServiceFormValues
): CreateServicePayload {
  return {
    title: values.title,
    slug: slugify(values.title),
    description: values.description || undefined,
    scheduleId: Number(values.scheduleId),
    price: values.price ? Number(values.price) : undefined,
    defaultDuration: Number(values.defaultDuration),
    beforeBuffer: Number(values.beforeBuffer),
    afterBuffer: Number(values.afterBuffer),
    minimumNotice: Number(values.minimumNotice),
    maximumAdvanceBooking: Number(values.maximumAdvanceBooking),
    capacity: Number(values.capacity),
    bookingWindowType: values.bookingWindowType,
  };
}
