import { z } from "zod";

import { RESOURCE_STATUSES } from "@/features/resources/types/resource";

const RESOURCE_NAME_MAX = 255;
const RESOURCE_SLUG_MAX = 255;

const optionalInteger = z
  .string()
  .trim()
  .optional()
  .refine(
    (value) => !value || Number.isInteger(Number(value)),
    "Capacity must be a whole number."
  );

export const createResourceFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, "Resource name is required.")
    .max(
      RESOURCE_NAME_MAX,
      `Must be at most ${RESOURCE_NAME_MAX} characters.`
    ),
  slug: z
    .string()
    .trim()
    .min(1, "Slug is required.")
    .max(
      RESOURCE_SLUG_MAX,
      `Must be at most ${RESOURCE_SLUG_MAX} characters.`
    )
    .regex(
      /^[a-z0-9]+(?:-[a-z0-9]+)*$/,
      "Use lowercase letters, numbers, and single hyphens."
    ),
  description: z.string().trim().optional(),
  resourceTypeId: z.string().min(1, "Select a resource type."),
  capacity: optionalInteger,
  status: z.enum(RESOURCE_STATUSES),
});

export type CreateResourceFormValues = z.infer<
  typeof createResourceFormSchema
>;

export const DEFAULT_CREATE_RESOURCE_VALUES: CreateResourceFormValues = {
  name: "",
  slug: "",
  description: "",
  resourceTypeId: "",
  capacity: "",
  status: "ACTIVE",
};

export function slugifyResourceName(name: string): string {
  return name
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, RESOURCE_SLUG_MAX);
}

export const createResourceTypeFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, "Type name is required.")
    .max(100, "Must be at most 100 characters."),
  description: z.string().trim().optional(),
  icon: z
    .string()
    .trim()
    .max(100, "Must be at most 100 characters.")
    .optional(),
});

export type CreateResourceTypeFormValues = z.infer<
  typeof createResourceTypeFormSchema
>;
