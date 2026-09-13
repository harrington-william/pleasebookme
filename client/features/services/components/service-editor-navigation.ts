import {
  CalendarClock,
  CircleCheck,
  Clock,
  DollarSign,
  Link2,
  type LucideIcon,
} from "lucide-react";

import type { ServiceFormValues } from "@/features/services/schemas/service-schema";

/**
 * The tab is a query param, not a route segment, because every tab edits one
 * aggregate saved by one request. A path segment would imply the sections are
 * separately addressable resources, and — the practical half — swapping the page
 * module on navigation unmounts the form and loses whatever was typed.
 */
export const SERVICE_TAB_QUERY_KEY = "tabName";

export type ServiceTabId =
  | "basics"
  | "availability"
  | "price"
  | "limits"
  | "confirmation";

export type ServiceEditorTab = {
  id: ServiceTabId;
  label: string;
  group: "Setup" | "Policies";
  icon: LucideIcon;
  /** Drives which tab a failed submit jumps to, so the error is on screen. */
  fields: readonly (keyof ServiceFormValues)[];
};

export const SERVICE_EDITOR_TABS: readonly ServiceEditorTab[] = [
  {
    id: "basics",
    label: "Basics",
    group: "Setup",
    icon: Link2,
    fields: ["title", "slug", "description", "location"],
  },
  {
    id: "availability",
    label: "Availability",
    group: "Setup",
    icon: CalendarClock,
    fields: ["scheduleId", "timezone"],
  },
  {
    id: "price",
    label: "Price & Duration",
    group: "Policies",
    icon: DollarSign,
    fields: ["price", "currency", "defaultDuration"],
  },
  {
    id: "limits",
    label: "Limits & Buffers",
    group: "Policies",
    icon: Clock,
    fields: [
      "beforeBuffer",
      "afterBuffer",
      "minimumNoticeAmount",
      "minimumNoticeUnit",
      "maximumAdvanceAmount",
      "maximumAdvanceUnit",
      "capacity",
      "bookingWindowType",
      "maxActiveBookingPerBooker",
    ],
  },
  {
    id: "confirmation",
    label: "Confirmation",
    group: "Policies",
    icon: CircleCheck,
    fields: ["requiresConfirmation", "disableCancelling", "successRedirectUrl"],
  },
];

export const DEFAULT_SERVICE_TAB: ServiceTabId = "basics";

export function resolveServiceTab(value: string | null): ServiceTabId {
  const match = SERVICE_EDITOR_TABS.find((tab) => tab.id === value);
  return match ? match.id : DEFAULT_SERVICE_TAB;
}

export function tabForField(
  field: keyof ServiceFormValues
): ServiceTabId | null {
  const match = SERVICE_EDITOR_TABS.find((tab) => tab.fields.includes(field));
  return match ? match.id : null;
}
