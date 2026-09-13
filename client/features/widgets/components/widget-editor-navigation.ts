import type { WidgetFormValues } from "@/features/widgets/schemas/widget-schema";

/**
 * The step is a query param, not a route segment, for the same reason the
 * services editor's tab is: all three steps edit one aggregate saved by one
 * request, and swapping the page module on navigation would unmount the form
 * and drop the generated key pair along with everything else typed.
 */
export const WIDGET_STEP_QUERY_KEY = "step";

export type WidgetStepId = "basics" | "security" | "review";

export type WidgetEditorStep = {
  id: WidgetStepId;
  numeral: string;
  label: string;
  description: string;
  /** Drives Continue's validation and which step a failed submit jumps to. */
  fields: readonly (keyof WidgetFormValues)[];
};

export const WIDGET_EDITOR_STEPS: readonly WidgetEditorStep[] = [
  {
    id: "basics",
    numeral: "01",
    label: "Basics",
    description: "Name and type",
    fields: ["name", "type", "enabled"],
  },
  {
    id: "security",
    numeral: "02",
    label: "Security",
    description: "Origin and keys",
    fields: ["origin", "credentials"],
  },
  {
    id: "review",
    numeral: "03",
    label: "Review",
    description: "Confirm and save",
    fields: [],
  },
];

export const DEFAULT_WIDGET_STEP: WidgetStepId = "basics";

export function resolveWidgetStep(value: string | null): WidgetStepId {
  const match = WIDGET_EDITOR_STEPS.find((step) => step.id === value);
  return match ? match.id : DEFAULT_WIDGET_STEP;
}

export function stepForField(
  field: keyof WidgetFormValues
): WidgetStepId | null {
  const match = WIDGET_EDITOR_STEPS.find((step) => step.fields.includes(field));
  return match ? match.id : null;
}

export function fieldsForStep(
  step: WidgetStepId
): readonly (keyof WidgetFormValues)[] {
  return WIDGET_EDITOR_STEPS.find((candidate) => candidate.id === step)?.fields ?? [];
}

function indexOfStep(step: WidgetStepId): number {
  return WIDGET_EDITOR_STEPS.findIndex((candidate) => candidate.id === step);
}

export function nextWidgetStep(step: WidgetStepId): WidgetStepId | null {
  return WIDGET_EDITOR_STEPS[indexOfStep(step) + 1]?.id ?? null;
}

export function previousWidgetStep(step: WidgetStepId): WidgetStepId | null {
  return WIDGET_EDITOR_STEPS[indexOfStep(step) - 1]?.id ?? null;
}
