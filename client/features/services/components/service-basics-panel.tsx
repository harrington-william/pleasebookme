"use client";

import { useFormContext } from "react-hook-form";

import { Input } from "@/components/ui/input";
import { useServiceEditor } from "@/features/services/components/service-editor-context";
import {
  Field,
  inputClassName,
  Section,
} from "@/features/services/components/service-form-primitives";
import {
  slugify,
  type ServiceFormValues,
} from "@/features/services/schemas/service-schema";
import { cn } from "@/lib/utils";

export function ServiceBasicsPanel() {
  const { mode } = useServiceEditor();
  const {
    register,
    setValue,
    formState: { errors },
  } = useFormContext<ServiceFormValues>();

  return (
    <Section id="basics" title="Basics">
      <Field label="Title" htmlFor="title" error={errors.title?.message}>
        <Input
          id="title"
          placeholder="Executive Consultation"
          aria-invalid={errors.title ? true : undefined}
          className="h-9 rounded-lg border-border bg-background px-md mt-sm text-body-md"
          {...register("title", {
            onBlur: (event) => {
              // The slug is frozen once a service exists: it is the public
              // booking URL, and re-deriving it from a retitle would break
              if (mode === "edit") return;

              setValue("slug", slugify(event.target.value));
            },
          })}
        />
      </Field>

      {/* Description */}
      <Field
        label="Description"
        htmlFor="description"
        error={errors.description?.message}
      >
        <textarea
          id="description"
          rows={4}
          placeholder="Briefly describe the service offering..."
          className={cn(inputClassName, "h-auto resize-none py-sm mt-sm")}
          {...register("description")}
        />
      </Field>

      <Field
        label="Service Cover Image"
      >
        <button
          type="button"
          disabled
          className="flex h-9 w-full cursor-not-allowed items-center justify-center rounded-lg border border-dashed border-border text-label-md text-muted-foreground opacity-50 mt-sm"
        >
          Upload Image
        </button>
      </Field>

      <Field
        label="Location"
        htmlFor="location"
        error={errors.location?.message}
      >
        <Input
          id="location"
          placeholder="123 Collins St, Melbourne"
          aria-invalid={errors.location ? true : undefined}
          className="h-9 rounded-lg border-border bg-background px-md mt-sm text-body-md"
          {...register("location")}
        />
      </Field>
    </Section>
  );
}
