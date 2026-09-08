"use client";

import { useFormContext } from "react-hook-form";

import { NativeSelect } from "@/components/form/native-select";
import {
  Field,
  inputClassName,
  Section,
} from "@/features/services/components/service-form-primitives";
import type { ServiceFormValues } from "@/features/services/schemas/service-schema";
import { CURRENCIES } from "@/features/services/types/service";
import { cn } from "@/lib/utils";

export function ServicePricePanel() {
  const {
    register,
    formState: { errors },
  } = useFormContext<ServiceFormValues>();

  return (
    <Section id="price" title="Price &amp; Duration">
      <div className="grid grid-cols-1 gap-md sm:grid-cols-2">

        {/* Price */}
        <Field label="Price" htmlFor="price" error={errors.price?.message}>
          <input
            id="price"
            type="text"
            inputMode="decimal"
            placeholder="0.00"
            aria-invalid={errors.price ? true : undefined}
            className={cn(inputClassName, "mt-sm")}
            {...register("price")}
          />
        </Field>

        {/* Currency */}
        <Field label="Currency" htmlFor="currency">
          <NativeSelect
            id="currency"
            containerClassName="w-full"
            className="w-full mt-sm"
            {...register("currency")}
          >
            {CURRENCIES.map((currency) => (
              <option key={currency} value={currency}>
                {currency}
              </option>
            ))}
          </NativeSelect>
        </Field>
      </div>

      {/* Duration */}
      <Field
        label="Duration"
        htmlFor="defaultDuration"
        error={errors.defaultDuration?.message}
      >
        <div className="relative mt-sm">
          <input
            id="defaultDuration"
            type="number"
            min={1}
            aria-invalid={errors.defaultDuration ? true : undefined}
            className={cn(inputClassName)}
            {...register("defaultDuration")}
          />
          <span className="pointer-events-none absolute top-1/2 right-md -translate-y-1/2 text-body-md text-muted-foreground">
            Minutes
          </span>
        </div>
      </Field>
    </Section>
  );
}
