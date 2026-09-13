/**
 * core.booking_policies stores minimum_notice and maximum_advance_booking as bare
 * INTEGERs with no unit column, so minutes is the canonical unit on the wire and
 * the amount/unit pair only ever exists in the form. Reading a value back picks
 * the largest unit that divides evenly, which is why 120 round-trips as "2 Hours"
 * rather than "120 Minutes".
 */

export const DURATION_UNITS = ["MINUTES", "HOURS", "DAYS"] as const;
export type DurationUnit = (typeof DURATION_UNITS)[number];

const MINUTES_PER_UNIT: Record<DurationUnit, number> = {
  MINUTES: 1,
  HOURS: 60,
  DAYS: 60 * 24,
};

export const DURATION_UNIT_LABELS: Record<DurationUnit, string> = {
  MINUTES: "Minutes",
  HOURS: "Hours",
  DAYS: "Days",
};

// Largest first, so fromMinutes reports the coarsest unit that fits exactly.
const UNITS_BY_SIZE: DurationUnit[] = ["DAYS", "HOURS", "MINUTES"];

export function isDurationUnit(value: string): value is DurationUnit {
  return (DURATION_UNITS as readonly string[]).includes(value);
}

export function toMinutes(
  amount: number,
  unit: DurationUnit
): number {
  return amount * MINUTES_PER_UNIT[unit];
}

export function fromMinutes(minutes: number): {
  amount: number;
  unit: DurationUnit;
} {
  if (minutes === 0) {
    return { amount: 0, unit: "MINUTES" };
  }

  const unit =
    UNITS_BY_SIZE.find((candidate) => minutes % MINUTES_PER_UNIT[candidate] === 0) ??
    "MINUTES";

  return { amount: minutes / MINUTES_PER_UNIT[unit], unit };
}

/**
 * Step sizes come straight from the product decision: minimum notice moves in
 * 15-minute increments, maximum advance in whole days. Hours are whole numbers in
 * both, so one table serves both fields.
 */
const AMOUNT_STEPS: Record<DurationUnit, { step: number; max: number }> = {
  MINUTES: { step: 15, max: 240 },
  HOURS: { step: 1, max: 48 },
  DAYS: { step: 1, max: 365 },
};

export function durationAmountOptions(
  unit: DurationUnit,
  includeZero = false
): number[] {
  const { step, max } = AMOUNT_STEPS[unit];
  const options: number[] = [];

  for (let value = includeZero ? 0 : step; value <= max; value += step) {
    options.push(value);
  }

  return options;
}

export function formatDurationAmount(
  amount: number,
  unit: DurationUnit
): string {
  if (amount === 0) return "None";

  const label = DURATION_UNIT_LABELS[unit].toLowerCase();
  return `${amount} ${amount === 1 ? label.slice(0, -1) : label}`;
}

/** Buffers are minutes-only on this platform, in the same 5-minute grid as availability. */
export const BUFFER_STEP_MINUTES = 5;
export const BUFFER_MAX_MINUTES = 120;

export const BUFFER_OPTIONS: number[] = Array.from(
  { length: BUFFER_MAX_MINUTES / BUFFER_STEP_MINUTES + 1 },
  (_, index) => index * BUFFER_STEP_MINUTES
);

export function formatBufferLabel(minutes: number): string {
  return minutes === 0 ? "No buffer" : `${minutes} minutes`;
}
