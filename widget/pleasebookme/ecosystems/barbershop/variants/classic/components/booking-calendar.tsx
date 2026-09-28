"use client";

import { ChevronLeft, ChevronRight } from "lucide-react";
import { useMemo, useState } from "react";

import {
  addDays,
  compareDates,
  monthGrid,
  todayInZone,
  weekdayOf,
  weekStartsOnFrom,
} from "../../../../../core/lib/date-math";
import type { WeekStart } from "../../../logic/types";
import { cn } from "../../../../../core/lib/cn";

import { TimezoneSelect } from "./timezone-select";

function monthParts(dateString: string): [number, number] {
  const [year, month] = dateString.split("-").map(Number);
  return [year, month];
}

function shiftMonth(year: number, month: number, amount: number): [number, number] {
  const index = year * 12 + month - 1 + amount;
  return [Math.floor(index / 12), (index % 12 + 12) % 12 + 1];
}

export function BookingCalendar({
  value,
  onChange,
  visitorZone,
  onZoneChange,
  weekStart,
  availableWeekdays,
  maximumAdvanceBooking,
}: {
  value: string | null;
  onChange: (date: string) => void;
  visitorZone: string;
  onZoneChange: (zone: string) => void;
  weekStart: WeekStart;
  availableWeekdays: number[];
  maximumAdvanceBooking: number;
}) {
  const today = todayInZone(visitorZone);
  const [initialYear, initialMonth] = monthParts(value ?? today);
  const [displayedMonth, setDisplayedMonth] = useState<[number, number]>([
    initialYear,
    initialMonth,
  ]);
  const [year, month] = displayedMonth;
  const maxDate = addDays(today, Math.ceil(maximumAdvanceBooking / 1440));
  const cells = useMemo(
    () => monthGrid(year, month, weekStartsOnFrom(weekStart)),
    [month, weekStart, year]
  );
  const weekdayLabels = weekStart === "SUNDAY"
    ? ["S", "M", "T", "W", "T", "F", "S"]
    : ["M", "T", "W", "T", "F", "S", "S"];
  const currentMonthKey = today.slice(0, 7);
  const displayKey = `${year}-${String(month).padStart(2, "0")}`;
  const next = shiftMonth(year, month, 1);
  const nextKey = `${next[0]}-${String(next[1]).padStart(2, "0")}-01`;

  return (
    <div className="rounded-xl border border-border bg-surface p-md shadow-sm md:p-lg">
      <div className="mb-lg flex items-center justify-between">
        <button
          type="button"
          disabled={displayKey <= currentMonthKey}
          onClick={() => setDisplayedMonth(shiftMonth(year, month, -1))}
          className="flex size-10 cursor-pointer items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-surface-hover hover:text-foreground disabled:cursor-not-allowed disabled:opacity-30"
          aria-label="Previous month"
        >
          <ChevronLeft className="size-5" />
        </button>
        <h2 className="text-headline-sm">
          {new Intl.DateTimeFormat(undefined, { month: "long", year: "numeric", timeZone: "UTC" }).format(new Date(Date.UTC(year, month - 1, 1)))}
        </h2>
        <button
          type="button"
          disabled={compareDates(nextKey, maxDate) > 0}
          onClick={() => setDisplayedMonth(next)}
          className="flex size-10 cursor-pointer items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-surface-hover hover:text-foreground disabled:cursor-not-allowed disabled:opacity-30"
          aria-label="Next month"
        >
          <ChevronRight className="size-5" />
        </button>
      </div>
      <div className="grid grid-cols-7 text-center">
        {weekdayLabels.map((label, index) => (
          <span key={`${label}-${index}`} className="py-xs text-label-md text-muted-foreground">
            {label}
          </span>
        ))}
      </div>
      <div className="mb-md grid grid-cols-7 gap-x-base gap-y-xs text-center">
        {cells.map((cell) => {
          const available =
            cell.inMonth &&
            compareDates(cell.dateString, today) >= 0 &&
            compareDates(cell.dateString, maxDate) <= 0 &&
            availableWeekdays.includes(weekdayOf(cell.dateString));
          const selected = value === cell.dateString;
          return (
            <button
              key={cell.dateString}
              type="button"
              disabled={!available}
              onClick={() => onChange(cell.dateString)}
              className={cn(
                "relative flex h-10 w-full items-center justify-center rounded-full text-body-md",
                available && "cursor-pointer transition-colors hover:bg-surface-hover",
                !available && "cursor-not-allowed text-muted-foreground opacity-30",
                selected && "bg-primary font-semibold text-primary-foreground"
              )}
            >
              {Number(cell.dateString.slice(-2))}
              {cell.dateString === today && available && !selected ? (
                <span className="absolute bottom-1 h-0.5 w-4 rounded-full bg-primary" />
              ) : null}
            </button>
          );
        })}
      </div>
      <TimezoneSelect value={visitorZone} onChange={onZoneChange} />
    </div>
  );
}
