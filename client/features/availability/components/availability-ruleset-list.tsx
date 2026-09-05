import { Globe } from "lucide-react";
import Link from "next/link";

import { DeleteScheduleButton } from "@/features/availability/components/delete-schedule-button";
import {
  DAY_DEFINITIONS,
  DAY_DISPLAY_ORDER,
  type Availability,
  type AvailabilityRuleset,
} from "@/features/availability/types/availability";

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

function formatTime(time: string): string {
  const [hourStr, minuteStr] = time.slice(0, 5).split(":");
  const hour = Number(hourStr);
  const period = hour >= 12 ? "PM" : "AM";
  const twelveHour = hour % 12 === 0 ? 12 : hour % 12;
  return `${twelveHour}:${minuteStr} ${period}`;
}

function formatWeeklySchedule(availabilities: Availability[]): string[] {
  if (availabilities.length === 0) return ["No hours configured"];

  return availabilities.map(
    (availability) =>
      `${formatDayRange(availability.days)}, ${formatTime(
        availability.startTime
      )} - ${formatTime(availability.endTime)}`
  );
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
    <div className="overflow-hidden rounded-xl border border-border bg-surface">
      {rulesets.map(({ schedule, availabilities }) => (
        <Link
          key={schedule.scheduleId}
          href={`/dashboard/availability/${schedule.scheduleId}`}
          className="flex flex-col gap-lg border-b border-border p-lg transition-colors last:border-0 hover:bg-surface-hover sm:flex-row sm:items-center sm:justify-between"
        >
          {/* Name + timezone */}
          <div className="flex flex-col gap-xs sm:w-72 sm:shrink-0">
            <span className="text-headline-md font-medium text-foreground">
              {schedule.title}
            </span>
            <span className="inline-flex items-center gap-xs text-body-md text-muted-foreground">
              <Globe className="size-4" aria-hidden="true" />
              {schedule.timezone}
            </span>
          </div>

          {/* Weekly schedule */}
          <div className="flex flex-1 flex-col gap-xs font-mono text-body-md text-muted-foreground">
            {formatWeeklySchedule(availabilities).map((line, index) => (
              <span key={index}>{line}</span>
            ))}
          </div>

          {/* Status + delete */}
          <div className="flex items-center gap-lg sm:shrink-0">
            <span
              aria-disabled="true"
              title="core.schedules has no enabled/status column — there is nothing to toggle yet."
              className="inline-flex h-6 w-11 items-center rounded-full bg-muted opacity-50"
            >
              <span className="ml-0.5 size-5 rounded-full bg-foreground/40" />
            </span>

            <DeleteScheduleButton
              scheduleId={schedule.scheduleId}
              title={schedule.title}
            />
          </div>
        </Link>
      ))}
    </div>
  );
}
