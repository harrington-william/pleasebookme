"use client";

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { cn } from "@/lib/utils";

const STEP_MINUTES = 5;
const MINUTES_IN_DAY = 24 * 60;

export function formatTimeLabel(value: string): string {
  const [rawHour, rawMinute] = value.split(":");
  const hour24 = Number(rawHour);
  const hour12 = hour24 % 12 === 0 ? 12 : hour24 % 12;

  return `${hour12}:${rawMinute}${hour24 < 12 ? "am" : "pm"}`;
}

export const TIME_OPTIONS: { value: string; label: string }[] = Array.from(
  { length: MINUTES_IN_DAY / STEP_MINUTES },
  (_, index) => {
    const minutes = index * STEP_MINUTES;
    const value = `${String(Math.floor(minutes / 60)).padStart(2, "0")}:${String(
      minutes % 60
    ).padStart(2, "0")}`;

    return { value, label: formatTimeLabel(value) };
  }
);

type TimeSelectProps = {
  value: string;
  onValueChange: (value: string) => void;
  onBlur?: () => void;
  name?: string;
  "aria-label": string;
  invalid?: boolean;
  className?: string;
};

export function TimeSelect({
  value,
  onValueChange,
  onBlur,
  name,
  invalid,
  className,
  "aria-label": ariaLabel,
}: TimeSelectProps) {
  return (
    <Select
      name={name}
      value={value}
      onValueChange={(next) => onValueChange(next as string)}
    >
      <SelectTrigger
        aria-label={ariaLabel}
        aria-invalid={invalid ? true : undefined}
        onBlur={onBlur}
        className={cn(
          "h-9 w-28 justify-center rounded-full border-border bg-background px-md text-body-md text-foreground",
          "hover:border-primary/60 dark:bg-background dark:hover:bg-background",
          "[&_svg]:hidden",
          className
        )}
      >
        <SelectValue>
          {(selected: string | null) =>
            selected ? formatTimeLabel(selected) : ""
          }
        </SelectValue>
      </SelectTrigger>

      <SelectContent
        align="start"
        alignItemWithTrigger={false}
        className="max-h-64 w-32 min-w-0"
      >
        {TIME_OPTIONS.map((option) => (
          <SelectItem key={option.value} value={option.value}>
            {option.label}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
