"use client";

import {
  Controller,
  type Control,
  type FieldErrors,
} from "react-hook-form";

import { Switch } from "@/components/ui/switch";
import { TimeSelect } from "@/features/availability/components/time-select";
import type { CreateAvailabilityFormValues } from "@/features/availability/schemas/availability-schema";
import { cn } from "@/lib/utils";

type WeekdayRowProps = {
  index: number;
  label: string;
  control: Control<CreateAvailabilityFormValues>;
  errors: FieldErrors<CreateAvailabilityFormValues>;
};

export function WeekdayRow({
  index,
  label,
  control,
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
              "flex flex-col gap-sm border-b border-border py-sm last:border-0 sm:flex-row sm:items-center",
              !enabled && "opacity-60"
            )}
          >
            <span className="w-32 shrink-0 text-label-md text-foreground">
              {label}
            </span>

            {enabled ? (
              <div className="flex flex-1 flex-wrap items-center gap-sm">
                <Controller
                  control={control}
                  name={`days.${index}.startTime`}
                  render={({ field: startField }) => (
                    <TimeSelect
                      name={startField.name}
                      value={startField.value}
                      onValueChange={startField.onChange}
                      onBlur={startField.onBlur}
                      invalid={dayErrors?.startTime ? true : undefined}
                      aria-label={`${label} start time`}
                    />
                  )}
                />

                <span className="text-label-md text-muted-foreground">-</span>

                <Controller
                  control={control}
                  name={`days.${index}.endTime`}
                  render={({ field: endField }) => (
                    <TimeSelect
                      name={endField.name}
                      value={endField.value}
                      onValueChange={endField.onChange}
                      onBlur={endField.onBlur}
                      invalid={dayErrors?.endTime ? true : undefined}
                      aria-label={`${label} end time`}
                    />
                  )}
                />

                {dayErrors?.endTime ? (
                  <span className="text-label-md text-destructive">
                    {dayErrors.endTime.message}
                  </span>
                ) : null}
              </div>
            ) : (
              <span className="flex-1 text-body-md text-muted-foreground italic">
                Unavailable
              </span>
            )}

            <Switch
              id={`day-${index}-enabled`}
              name={field.name}
              inputRef={field.ref}
              checked={enabled}
              onCheckedChange={(checked) => field.onChange(checked)}
              onBlur={field.onBlur}
              aria-label={`Enable ${label}`}
              className="ml-auto shrink-0 sm:ml-sm"
            />
          </div>
        );
      }}
    />
  );
}
