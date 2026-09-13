import { z } from "zod";

import {
  WIDGET_EDITABLE_STATUSES,
  WIDGET_TYPES,
  type Widget,
  type WidgetCredentials,
} from "@/features/widgets/types/widget";

const NAME_MAX = 255;
const ORIGIN_MAX = 255;

// The fixed formats the platform mints; anything else is rejected there with a 400.
export const PUBLIC_KEY_PATTERN = /^pbm_pk_[A-Za-z0-9_-]{22}$/;
export const SECRET_KEY_PATTERN = /^pbm_sk_[A-Za-z0-9_-]{43}$/;

/**
 * UX guardrail only. The platform normalises the value to scheme://host[:port]
 * and is the authority on what is acceptable — this just stops obvious typos
 * from costing a round trip.
 */
export const ORIGIN_PATTERN =
  /^(https?:\/\/)?[a-z0-9-]+(\.[a-z0-9-]+)*(:\d{1,5})?(\/.*)?$/i;

export const ORIGIN_PLACEHOLDER = "https://barbershop.com";

/**
 * Display-only. The platform stores null when no origin is registered; the
 * card shows the product host in its place because the brief asks for it, and
 * the tooltip says what null actually means.
 */
export const DEFAULT_ORIGIN_LABEL = "pleasebookmee.com";

const credentialsSchema = z.object({
  publicKey: z.string().regex(PUBLIC_KEY_PATTERN, {
    error: "Public key is not in the expected format",
  }),
  secretKey: z.string().regex(SECRET_KEY_PATTERN, {
    error: "Secret key is not in the expected format",
  }),
});

export const widgetFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, { error: "Name is required" })
    .max(NAME_MAX, { error: `Keep the name under ${NAME_MAX} characters` }),
  type: z.enum(WIDGET_TYPES),
  enabled: z.boolean(),
  origin: z
    .string()
    .trim()
    .max(ORIGIN_MAX, { error: `Keep the origin under ${ORIGIN_MAX} characters` })
    .refine((value) => value === "" || ORIGIN_PATTERN.test(value), {
      error: `Enter a website like ${ORIGIN_PLACEHOLDER}`,
    }),
  credentials: credentialsSchema.nullable(),
});

export type WidgetFormValues = z.infer<typeof widgetFormSchema>;

// Create needs a pair; edit does not. Same object, one extra rule.
export const createWidgetFormSchema = widgetFormSchema.refine(
  (values) => values.credentials !== null,
  { error: "Generate a key pair first", path: ["credentials"] }
);

export const DEFAULT_WIDGET_FORM_VALUES: WidgetFormValues = {
  name: "",
  type: "INLINE",
  enabled: true,
  origin: "",
  credentials: null,
};

export function toEditWidgetFormValues(widget: Widget): WidgetFormValues {
  return {
    name: widget.name,
    type: widget.type,
    enabled: widget.status === "ACTIVE",
    origin: widget.origin ?? "",
    credentials: null,
  };
}

// Wire payloads — what the BFF re-validates before forwarding to the platform.
export const widgetCreatePayloadSchema = z.object({
  name: z.string().trim().min(1).max(NAME_MAX),
  type: z.enum(WIDGET_TYPES),
  origin: z.string().max(ORIGIN_MAX).nullable(),
  credentials: credentialsSchema,
});

export const widgetUpdatePayloadSchema = z.object({
  name: z.string().trim().min(1).max(NAME_MAX),
  type: z.enum(WIDGET_TYPES),
  status: z.enum(WIDGET_EDITABLE_STATUSES),
  origin: z.string().max(ORIGIN_MAX).nullable(),
  credentials: credentialsSchema.nullable(),
});

export type WidgetCreatePayload = z.infer<typeof widgetCreatePayloadSchema>;
export type WidgetUpdatePayload = z.infer<typeof widgetUpdatePayloadSchema>;

/** Form values whose credentials the caller has already checked are present. */
export type WidgetFormValuesWithCredentials = WidgetFormValues & {
  credentials: WidgetCredentials;
};

export function toWidgetCreatePayload(
  values: WidgetFormValuesWithCredentials
): WidgetCreatePayload {
  return {
    name: values.name,
    type: values.type,
    origin: values.origin === "" ? null : values.origin,
    credentials: values.credentials,
  };
}

export function toWidgetUpdatePayload(
  values: WidgetFormValues
): WidgetUpdatePayload {
  return {
    name: values.name,
    type: values.type,
    status: values.enabled ? "ACTIVE" : "DISABLED",
    origin: values.origin === "" ? null : values.origin,
    credentials: values.credentials,
  };
}
