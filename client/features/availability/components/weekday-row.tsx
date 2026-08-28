"use client";

import {
  Controller,
  type Control,
  type FieldErrors,
  type UseFormRegister,
} from "react-hook-form";

import { Checkbox } from "@/components/ui/checkbox";
import type { CreateAvailabilityFormValues } from "@/features/availability/schemas/availability-schema";
import { cn } from "@/lib/utils";

type WeekdayRowProps = {
  index: number;
  label: string;
  control: Control<CreateAvailabilityFormValues>;
  register: UseFormRegister<CreateAvailabilityFormValues>;
  errors: FieldErrors<CreateAvailabilityFormValues>;
};

const timeInputClassName =
  "w-28 rounded-lg border border-border bg-background px-sm py-xs text-center font-mono text-mono-label text-foreground focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none";

export function WeekdayRow({
  index,
  label,
  control,
  register,
  errors,
}: WeekdayRowProps) {
  const dayErrors = errors.days?.[index];

  return (
    <Controller
      control={control}
      name={`days.${index}.enabled`}
      render={({ field }) => {
        const enabled = field.value;

        return (
          <div
            className={cn(
              "flex flex-col gap-sm border-b border-border py-sm last:border-0 sm:flex-row sm:items-start",
              !enabled && "opacity-60"
            )}
          >
            <div className="flex w-32 shrink-0 items-center justify-between pt-1">
              <span className="text-label-md text-foreground">{label}</span>
              <Checkbox
                id={`day-${index}-enabled`}
                name={field.name}
                inputRef={field.ref}
                checked={enabled}
                onCheckedChange={(checked) => field.onChange(checked)}
                onBlur={field.onBlur}
                aria-label={`Enable ${label}`}
              />
            </div>

            {enabled ? (
              <div className="flex flex-1 flex-wrap items-center gap-sm">
                <input
                  type="time"
                  aria-label={`${label} start time`}
                  aria-invalid={dayErrors?.startTime ? true : undefined}
                  className={timeInputClassName}
                  {...register(`days.${index}.startTime`)}
                />
                <span className="text-label-md text-muted-foreground">-</span>
                <input
                  type="time"
                  aria-label={`${label} end time`}
                  aria-invalid={dayErrors?.endTime ? true : undefined}
                  className={timeInputClassName}
                  {...register(`days.${index}.endTime`)}
                />
                {dayErrors?.endTime ? (
                  <span className="text-label-md text-destructive">
                    {dayErrors.endTime.message}
                  </span>
                ) : null}
              </div>
            ) : (
              <span className="pt-1 text-body-md text-muted-foreground italic">
                Unavailable
              </span>
            )}
          </div>
        );
      }}
    />
  );
}
