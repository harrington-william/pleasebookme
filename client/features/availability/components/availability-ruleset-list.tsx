import { Info } from "lucide-react";

import { DeleteScheduleButton } from "@/features/availability/components/delete-schedule-button";
import {
  DAY_DEFINITIONS,
  DAY_DISPLAY_ORDER,
  type Availability,
  type AvailabilityRuleset,
} from "@/features/availability/types/availability";
import Link from "next/link";

const SHORT_LABELS = new Map<number, string>(
  DAY_DEFINITIONS.map((day) => [day.value, day.short])
);

function formatDayRange(days: number[]): string {
  if (days.length === 0) return "";

  const ordered = [...new Set(days)].sort(
    (a, b) => DAY_DISPLAY_ORDER.indexOf(a) - DAY_DISPLAY_ORDER.indexOf(b)
  );

  const segments: string[] = [];
  let start = ordered[0];
  let prev = ordered[0];

  for (let i = 1; i <= ordered.length; i += 1) {
    const current = ordered[i];
    const consecutive =
      current !== undefined &&
      DAY_DISPLAY_ORDER.indexOf(current) === DAY_DISPLAY_ORDER.indexOf(prev) + 1;

    if (!consecutive) {
      segments.push(
        start === prev
          ? SHORT_LABELS.get(start)!
          : `${SHORT_LABELS.get(start)}-${SHORT_LABELS.get(prev)}`
      );
      if (current !== undefined) start = current;
    }
    if (current !== undefined) prev = current;
  }

  return segments.join(", ");
}

function formatWeeklySchedule(availabilities: Availability[]): string {
  if (availabilities.length === 0) return "No hours configured";

  return availabilities
    .map(
      (availability) =>
        `${formatDayRange(availability.days)}, ${availability.startTime.slice(0, 5)} - ${availability.endTime.slice(0, 5)}`
    )
    .join("; ");
}

export function AvailabilityRulesetList({
  rulesets,
}: {
  rulesets: AvailabilityRuleset[];
}) {
  if (rulesets.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-border px-md py-2xl text-center">
        <p className="text-body-md text-muted-foreground">
          No availability rulesets yet. Create one to define your working hours.
        </p>
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-xl border border-border bg-surface cursor-pointer">
      <div className="overflow-x-auto">
        <table className="w-full border-collapse text-left">
          {/* Header */}
          <thead>
            <tr className="border-b border-border">
              <th className="px-md py-sm text-label-md tracking-wider text-muted-foreground uppercase">
                Name
              </th>
              <th className="px-md py-sm text-label-md tracking-wider text-muted-foreground uppercase">
                Weekly Schedule
              </th>
              <th
                className="px-md py-sm text-label-md tracking-wider text-muted-foreground uppercase"
                title="Requires linking a Service to this schedule (core.services.schedule_id) — the services feature has not been built yet."
              >
                Applied Services
              </th>
              <th
                className="px-md py-sm text-label-md tracking-wider text-muted-foreground uppercase"
                title="core.schedules has no enabled/status column — there is nothing to toggle yet."
              >
                Status
              </th>
              <th className="w-12 px-md py-sm" />
            </tr>
          </thead>

          {/* Body */}
          <tbody className="font-mono text-mono-label">
            {rulesets.map(({ schedule, availabilities }) => (
              <tr
                key={schedule.scheduleId}
                className="border-b border-border transition-colors last:border-0 hover:bg-surface-hover"
              >
                <td className="px-md py-sm">
                  <div className="font-sans text-body-md font-medium text-foreground">
                    {schedule.title}
                  </div>
                  <div className="mt-0.5 text-xs text-muted-foreground">
                    {schedule.timezone}
                  </div>
                </td>
                <td className="px-md py-sm text-muted-foreground">
                  {formatWeeklySchedule(availabilities)}
                </td>
                <td
                  className="px-md py-sm text-muted-foreground"
                  title="Requires linking a Service to this schedule — the services feature has not been built yet."
                >
                  <span className="inline-flex items-center gap-1">
                    <Info className="size-3.5" aria-hidden="true" />
                    Not tracked yet
                  </span>
                </td>
                <td
                  className="px-md py-sm text-muted-foreground"
                  title="core.schedules has no enabled/status column — there is nothing to toggle yet."
                >
                  <span
                    aria-disabled="true"
                    className="inline-flex h-5 w-9 items-center rounded-full bg-muted opacity-50"
                  >
                    <span className="ml-0.5 size-4 rounded-full bg-foreground/40" />
                  </span>
                </td>
                <td className="px-md py-sm text-right">
                  <DeleteScheduleButton
                    scheduleId={schedule.scheduleId}
                    title={schedule.title}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
