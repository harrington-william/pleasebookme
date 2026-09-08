"use client";

import { Controller, useFormContext, type Control } from "react-hook-form";

import { NativeSelect } from "@/components/form/native-select";
import {
  Field,
  inputClassName,
  Section,
} from "@/features/services/components/service-form-primitives";
import {
  BUFFER_OPTIONS,
  DURATION_UNITS,
  DURATION_UNIT_LABELS,
  durationAmountOptions,
  formatBufferLabel,
  formatDurationAmount,
  type DurationUnit,
} from "@/features/services/schemas/duration-unit";
import type { ServiceFormValues } from "@/features/services/schemas/service-schema";
import { BOOKING_WINDOW_TYPES } from "@/features/services/types/service";
import { cn } from "@/lib/utils";

/**
 * A stored value can sit off the option grid — a 7-minute notice, or an amount
 * that was valid in its previous unit. Merging it in keeps it selectable instead
 * of silently snapping the user's saved setting to something else on first render.
 */
function amountOptionsWith(
  current: number,
  unit: DurationUnit,
  includeZero: boolean
): number[] {
  const options = durationAmountOptions(unit, includeZero);
  if (Number.isNaN(current) || options.includes(current)) return options;

  return [...options, current].sort((a, b) => a - b);
}

function DurationPairField({
  label,
  hint,
  amountName,
  unitName,
  includeZero,
  control,
  error,
}: {
  label: string;
  hint?: string;
  amountName: "minimumNoticeAmount" | "maximumAdvanceAmount";
  unitName: "minimumNoticeUnit" | "maximumAdvanceUnit";
  includeZero: boolean;
  control: Control<ServiceFormValues>;
  error?: string;
}) {
  return (
    <Field label={label} hint={hint} error={error}>
      <div className="grid grid-cols-2 gap-sm mt-sm">
        <Controller
          name={unitName}
          control={control}
          render={({ field: unitField }) => (
            <Controller
              name={amountName}
              control={control}
              render={({ field: amountField }) => {
                const unit = unitField.value;
                const amount = Number(amountField.value);

                return (
                  <>
                    <NativeSelect
                      aria-label={`${label} amount`}
                      containerClassName="w-full"
                      className="w-full"
                      value={amountField.value}
                      onChange={(event) =>
                        amountField.onChange(event.target.value)
                      }
                      onBlur={amountField.onBlur}
                    >
                      {amountOptionsWith(amount, unit, includeZero).map(
                        (option) => (
                          <option key={option} value={option}>
                            {formatDurationAmount(option, unit)}
                          </option>
                        )
                      )}
                    </NativeSelect>

                    <NativeSelect
                      aria-label={`${label} unit`}
                      containerClassName="w-full"
                      className="w-full"
                      value={unit}
                      onChange={(event) => {
                        const nextUnit = event.target.value as DurationUnit;
                        unitField.onChange(nextUnit);

                        // Snap to the closest option at or below the current
                        // amount, so switching units never leaves the select
                        // showing a value its own option list does not contain.
                        const options = durationAmountOptions(
                          nextUnit,
                          includeZero
                        );
                        if (!options.includes(amount)) {
                          const below = options.filter(
                            (option) => option <= amount
                          );
                          amountField.onChange(
                            String(below.at(-1) ?? options[0])
                          );
                        }
                      }}
                    >
                      {DURATION_UNITS.map((option) => (
                        <option key={option} value={option}>
                          {DURATION_UNIT_LABELS[option]}
                        </option>
                      ))}
                    </NativeSelect>
                  </>
                );
              }}
            />
          )}
        />
      </div>
    </Field>
  );
}

export function ServiceLimitsPanel() {
  const {
    control,
    register,
    formState: { errors },
  } = useFormContext<ServiceFormValues>();

  return (
    <>
      <Section id="buffers" title="Buffers &amp; Notice">
        <div className="grid grid-cols-1 gap-md sm:grid-cols-2">

          {/* Before Buffer */}
          <Field
            label="Before event"
            htmlFor="beforeBuffer"
            error={errors.beforeBuffer?.message}
          >
            <NativeSelect
              id="beforeBuffer"
              containerClassName="w-full"
              className="w-full mt-sm"
              {...register("beforeBuffer")}
            >
              {BUFFER_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  {formatBufferLabel(option)}
                </option>
              ))}
            </NativeSelect>
          </Field>

          {/* After Buffer */}
          <Field
            label="After event"
            htmlFor="afterBuffer"
            error={errors.afterBuffer?.message}
          >
            <NativeSelect
              id="afterBuffer"
              containerClassName="w-full"
              className="w-full mt-sm"
              {...register("afterBuffer")}
            >
              {BUFFER_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  {formatBufferLabel(option)}
                </option>
              ))}
            </NativeSelect>
          </Field>
        </div>

        <div className="grid grid-cols-1 gap-md sm:grid-cols-2">

          {/* Minimum Notice */}
          <DurationPairField
            label="Minimum notice"
            amountName="minimumNoticeAmount"
            unitName="minimumNoticeUnit"
            includeZero
            control={control}
            error={errors.minimumNoticeAmount?.message}
          />

          {/* Maximum Advance Booking */}
          <DurationPairField
            label="Maximum advance"
            amountName="maximumAdvanceAmount"
            unitName="maximumAdvanceUnit"
            includeZero={false}
            control={control}
            error={errors.maximumAdvanceAmount?.message}
          />
        </div>
      </Section>

      <Section id="limits" title="Limits">
        <div className="grid grid-cols-1 gap-md sm:grid-cols-2">

          {/* Capacity */}
          <Field
            label="Capacity"
            htmlFor="capacity"
            error={errors.capacity?.message}
          >
            <input
              id="capacity"
              type="number"
              min={1}
              aria-invalid={errors.capacity ? true : undefined}
              className={cn(inputClassName, "mt-sm")}
              {...register("capacity")}
            />
          </Field>

          {/* Booking Window */}
          <Field label="Booking window" htmlFor="bookingWindowType">
            <NativeSelect
              id="bookingWindowType"
              containerClassName="w-full"
              className="w-full mt-sm"
              {...register("bookingWindowType")}
            >
              {BOOKING_WINDOW_TYPES.map((option) => (
                <option key={option} value={option}>
                  {option === "ROLLING"
                    ? "Rolling"
                    : "Fixed"}
                </option>
              ))}
            </NativeSelect>
          </Field>
        </div>

        {/* Max Active Booking per Booker */}
        <Field
          label="Max active bookings per booker"
          htmlFor="maxActiveBookingPerBooker"
          error={errors.maxActiveBookingPerBooker?.message}
        >
          <input
            id="maxActiveBookingPerBooker"
            type="number"
            min={1}
            placeholder="No limit"
            aria-invalid={errors.maxActiveBookingPerBooker ? true : undefined}
            className={cn(inputClassName, "mt-sm")}
            {...register("maxActiveBookingPerBooker")}
          />
        </Field>
      </Section>
    </>
  );
}
