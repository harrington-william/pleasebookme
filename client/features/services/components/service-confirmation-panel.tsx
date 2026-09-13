"use client";

import { Controller, useFormContext, type Control } from "react-hook-form";

import { Switch } from "@/components/ui/switch";
import {
  Field,
  inputClassName,
  Section,
} from "@/features/services/components/service-form-primitives";
import {
  SUCCESS_REDIRECT_URL_PLACEHOLDER,
  type ServiceFormValues,
} from "@/features/services/schemas/service-schema";
import { cn } from "@/lib/utils";

function ToggleRow({
  name,
  label,
  hint,
  control,
}: {
  name: "requiresConfirmation" | "disableCancelling";
  label: string;
  hint: string;
  control: Control<ServiceFormValues>;
}) {
  return (
    <div className="flex items-start justify-between gap-md rounded-lg border border-border bg-background px-md py-sm">
      <div className="space-y-base">
        <p className="text-body-md text-foreground">{label}</p>
        <p className="text-label-md text-muted-foreground">{hint}</p>
      </div>

      <Controller
        name={name}
        control={control}
        render={({ field }) => (
          <Switch
            aria-label={label}
            className="cursor-pointer"
            checked={field.value}
            onCheckedChange={(checked) => field.onChange(checked)}
            inputRef={field.ref}
          />
        )}
      />
    </div>
  );
}

export function ServiceConfirmationPanel() {
  const {
    control,
    register,
    formState: { errors },
  } = useFormContext<ServiceFormValues>();

  return (
    <Section id="confirmation" title="Confirmation &amp; Cancellation">

      {/* Requires Confirmation */}
      <ToggleRow
        name="requiresConfirmation"
        label="Requires confirmation"
        hint="Bookings wait for your approval instead of being accepted straight away."
        control={control}
      />

      {/* Disable Cancelling */}
      <ToggleRow
        name="disableCancelling"
        label="Disable cancelling"
        hint="Bookers cannot cancel once a booking is made."
        control={control}
      />

      {/* Redirect URL */}
      <Field
        label="Success redirect URL"
        htmlFor="successRedirectUrl"
        error={errors.successRedirectUrl?.message}
      >
        <input
          id="successRedirectUrl"
          type="url"
          placeholder={SUCCESS_REDIRECT_URL_PLACEHOLDER}
          aria-invalid={errors.successRedirectUrl ? true : undefined}
          className={cn(inputClassName, "mt-sm")}
          {...register("successRedirectUrl")}
        />
      </Field>
    </Section>
  );
}
