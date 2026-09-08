"use client";

import Link from "next/link";
import { useFormContext } from "react-hook-form";

import { NativeSelect } from "@/components/form/native-select";
import { useServiceEditor } from "@/features/services/components/service-editor-context";
import {
  Field,
  Section,
} from "@/features/services/components/service-form-primitives";
import type { ServiceFormValues } from "@/features/services/schemas/service-schema";

export function ServiceAvailabilityPanel() {
  const { schedules, timezones, onTimezoneTouched } = useServiceEditor();
  const {
    register,
    formState: { errors },
  } = useFormContext<ServiceFormValues>();

  return (
    <Section id="availability" title="Availability">
      <Field
        label="Availability Ruleset"
        htmlFor="scheduleId"
        error={errors.scheduleId?.message}
      >
        {schedules.length === 0 ? (
          <p className="rounded-lg border border-dashed border-border px-md py-sm text-body-md text-muted-foreground">
            You have no availability rulesets yet.{" "}
            <Link
              href="/dashboard/availability/new"
              className="text-primary hover:underline"
            >
              Create one first
            </Link>{" "}
            — every service requires a schedule (
            <span className="font-mono text-mono-label">
              core.services.schedule_id
            </span>
            ).
          </p>
        ) : (
          <NativeSelect
            id="scheduleId"
            aria-invalid={errors.scheduleId ? true : undefined}
            containerClassName="w-full"
            className="w-full mt-sm"
            {...register("scheduleId")}
          >
            <option value="">Select a ruleset…</option>
            {schedules.map((schedule) => (
              <option key={schedule.scheduleId} value={schedule.scheduleId} className="w-full">
                {schedule.title}
              </option>
            ))}
          </NativeSelect>
        )}
      </Field>

      <Field
        label="Timezone"
        htmlFor="timezone"
        error={errors.timezone?.message}
      >
        <NativeSelect
          id="timezone"
          aria-invalid={errors.timezone ? true : undefined}
          containerClassName="w-full"
          className="w-full mt-sm"
          {...register("timezone", {
            onChange: () => onTimezoneTouched(),
          })}
        >
          {timezones.map((zone) => (
            <option key={zone} value={zone}>
              {zone}
            </option>
          ))}
        </NativeSelect>
      </Field>
    </Section>
  );
}
