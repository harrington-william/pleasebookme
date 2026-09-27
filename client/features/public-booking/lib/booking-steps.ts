export type BookingStep =
  | "service"
  | "date"
  | "time"
  | "details"
  | "review"
  | "done";

export const BOOKING_STEPS: { key: BookingStep; label: string }[] = [
  { key: "service", label: "Service" },
  { key: "date", label: "Date" },
  { key: "time", label: "Time" },
  { key: "details", label: "Details" },
  { key: "review", label: "Review" },
  { key: "done", label: "Done" },
];

export function stepIndex(step: BookingStep): number {
  return BOOKING_STEPS.findIndex((item) => item.key === step);
}

export function stepNumber(step: BookingStep): number {
  return stepIndex(step) + 1;
}

export function stepLabel(step: BookingStep): string {
  return BOOKING_STEPS[stepIndex(step)].label;
}
