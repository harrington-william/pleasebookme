import { z } from "zod";

const phoneRegex = /^(\+?\d{9,15})$/;

export const bookingDetailsSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, { error: "Name is required" })
    .max(255, { error: "Name is too long" }),
  phone: z
    .string()
    .trim()
    .min(1, { error: "Phone number is required" })
    .max(50, { error: "Phone number is too long" })
    .regex(phoneRegex, { error: "Enter a valid phone number" }),
  email: z
    .union([
      z.literal(""),
      z.email({ error: "Enter a valid email" }).max(255),
    ])
    .optional(),
  notes: z.string().trim().max(1000).optional(),
});

export type BookingDetails = z.infer<typeof bookingDetailsSchema>;

export const bookingRequestSchema = bookingDetailsSchema.extend({
  timezone: z.string().max(100).optional(),
  slotStart: z.iso.datetime(),
});
