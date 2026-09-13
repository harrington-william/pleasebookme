import { z } from "zod";

import {
  DAY_DEFINITIONS,
  type AvailabilityRuleset,
  type AvailabilityWindow,
  type CreateAvailabilityRulesetInput,
} from "@/features/availability/types/availability";

const TITLE_MAX = 255;

const dayEntrySchema = z.object({
  value: z.number().int().min(0).max(6),
  enabled: z.boolean(),
  startTime: z.string().min(1, "Start time is required."),
  endTime: z.string().min(1, "End time is required."),
});

export const createAvailabilityFormSchema = z
  .object({
    title: z
      .string()
      .trim()
      .min(1, "Name is required.")
      .max(TITLE_MAX, `Must be at most ${TITLE_MAX} characters.`),
    timezone: z.string().trim().min(1, "Select a timezone."),
    days: z.array(dayEntrySchema).length(7),
  })
  .superRefine((values, ctx) => {
    values.days.forEach((day, index) => {
      if (day.enabled && day.startTime >= day.endTime) {
        ctx.addIssue({
          code: "custom",
          message: "End time must be after start time.",
          path: ["days", index, "endTime"],
        });
      }
    });
  });

export type CreateAvailabilityFormValues = z.infer<
  typeof createAvailabilityFormSchema
>;

// MON - FRI | 9AM - 5PM
export const DEFAULT_DAY_VALUES: CreateAvailabilityFormValues["days"] =
  DAY_DEFINITIONS.map((day) => ({
    value: day.value,
    enabled: day.value !== 0 && day.value !== 6,
    startTime: "09:00",
    endTime: "17:00",
  }));

function toLocalTimeString(value: string): string {
  return value.length === 5 ? `${value}:00` : value;
}

// Groups windows with same start/end times
export function toAvailabilityWindows(
  values: CreateAvailabilityFormValues
): AvailabilityWindow[] {
  const groups = new Map<string, number[]>();

  for (const day of values.days) {
    if (!day.enabled) continue;

    const startTime = toLocalTimeString(day.startTime);
    const endTime = toLocalTimeString(day.endTime);
    const key = `${startTime}|${endTime}`;

    const existing = groups.get(key);
    if (existing) {
      existing.push(day.value);
    } else {
      groups.set(key, [day.value]);
    }
  }

  return Array.from(groups.entries()).map(([key, days]) => {
    const [startTime, endTime] = key.split("|");
    return { days, startTime, endTime };
  });
}

export function toCreateAvailabilityRulesetInput(
  values: CreateAvailabilityFormValues
): CreateAvailabilityRulesetInput {
  return {
    title: values.title,
    timezone: values.timezone,
    windows: toAvailabilityWindows(values),
  };
}

// Inverse of toAvailabilityWindows — reconstructs per-day form state from
// the persisted Availability rows so the edit form starts pre-filled.
export function toEditAvailabilityFormValues(
  ruleset: AvailabilityRuleset
): CreateAvailabilityFormValues {
  const byDay = new Map<number, { startTime: string; endTime: string }>();

  for (const availability of ruleset.availabilities) {
    const startTime = availability.startTime.slice(0, 5);
    const endTime = availability.endTime.slice(0, 5);
    for (const day of availability.days) {
      byDay.set(day, { startTime, endTime });
    }
  }

  return {
    title: ruleset.schedule.title,
    timezone: ruleset.schedule.timezone,
    days: DAY_DEFINITIONS.map((day) => {
      const window = byDay.get(day.value);
      return {
        value: day.value,
        enabled: window !== undefined,
        startTime: window?.startTime ?? "09:00",
        endTime: window?.endTime ?? "17:00",
      };
    }),
  };
}
