"use client";

import { Controller, useFormContext } from "react-hook-form";

import { Switch } from "@/components/ui/switch";
import {
  Field,
  inputClassName,
} from "@/features/services/components/service-form-primitives";
import { WidgetStepSection } from "@/features/widgets/components/widget-step-section";
import { WidgetTypePicker } from "@/features/widgets/components/widget-type-picker";
import type { WidgetFormValues } from "@/features/widgets/schemas/widget-schema";

export function WidgetBasicsStep({ mode }: { mode: "create" | "edit" }) {
  const {
    control,
    register,
    formState: { errors },
  } = useFormContext<WidgetFormValues>();

  return (
    <WidgetStepSection id="basics" eyebrow="Step 01 · Basics" title="Basics">

      {/* Name */}
      <Field label="Widget name" htmlFor="name" error={errors.name?.message}>
        <input
          id="name"
          type="text"
          placeholder="Main website booking"
          autoComplete="off"
          aria-invalid={errors.name ? true : undefined}
          className={inputClassName}
          {...register("name")}
        />
      </Field>

      {/* Type */}
      <Field label="Widget type" error={errors.type?.message}>
        <WidgetTypePicker />
      </Field>

      {/* Enabled — a widget is always created ACTIVE, so this is only meaningful on edit */}
      {mode === "edit" ? (
        <div className="flex items-start justify-between gap-md rounded-lg border border-border bg-background px-md py-sm">
          <div className="space-y-base">
            <p className="text-body-md text-foreground">Widget enabled</p>
            <p className="text-label-md text-muted-foreground">
              Disabled widgets stop authenticating but keep their keys.
            </p>
          </div>

          <Controller
            name="enabled"
            control={control}
            render={({ field }) => (
              <Switch
                aria-label="Widget enabled"
                className="cursor-pointer"
                checked={field.value}
                onCheckedChange={(checked) => field.onChange(checked)}
                inputRef={field.ref}
              />
            )}
          />
        </div>
      ) : null}
    </WidgetStepSection>
  );
}
