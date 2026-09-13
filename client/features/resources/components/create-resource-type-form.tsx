"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { LoaderCircle } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useForm } from "react-hook-form";

import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { AuthFormAlert } from "@/features/auth/components/auth-form-alert";
import {
  createResourceTypeFormSchema,
  type CreateResourceTypeFormValues,
} from "@/features/resources/schemas/resource-schema";
import { createResourceType } from "@/features/resources/services/resource-api";
import { ApiRequestError } from "@/lib/api-error";

const labelClassName =
  "mb-sm text-label-md tracking-wider text-muted-foreground uppercase";

export function CreateResourceTypeForm() {
  const router = useRouter();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<CreateResourceTypeFormValues>({
    resolver: zodResolver(createResourceTypeFormSchema),
    mode: "onBlur",
    defaultValues: { name: "", description: "", icon: "" },
  });

  async function onSubmit(values: CreateResourceTypeFormValues) {
    setSubmitError(null);

    try {
      await createResourceType(values);
      router.push("/dashboard/resources/new");
      router.refresh();
    } catch (error) {
      setSubmitError(
        error instanceof ApiRequestError
          ? error.message
          : "Could not create this resource type. Please try again."
      );
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-lg">
      {submitError ? <AuthFormAlert message={submitError} /> : null}

      {/* Type Name */}
      <section className="space-y-md rounded-xl border border-border bg-surface p-lg">
        <div className="space-y-base">
          <Label htmlFor="name" className={labelClassName}>
            Name
          </Label>

          <Input
            id="name"
            placeholder="Meeting Room"
            aria-invalid={errors.name ? true : undefined}
            className="h-9 rounded-lg border-border bg-background px-md text-body-md"
            {...register("name")}
          />
          {errors.name ? (
            <p className="text-label-md text-destructive">
              {errors.name.message}
            </p>
          ) : null}
        </div>

        {/* Description */}
        <div className="space-y-base">
          <Label htmlFor="description" className={labelClassName}>
            Description
          </Label>

          <textarea
            id="description"
            rows={4}
            placeholder="Optional description"
            className="
            w-full resize-none rounded-lg border border-border bg-background px-md py-sm
            text-body-md text-foreground placeholder:text-muted-foreground
            focus-visible:border-primary focus-visible:ring-2
            focus-visible:ring-ring focus-visible:outline-none"
            {...register("description")}
          />
        </div>
        
        {/* Icon */}
        <div className="space-y-base">
          <Label htmlFor="icon" className={labelClassName}>
            Icon Name
          </Label>

          <Input
            id="icon"
            placeholder="Optional"
            aria-invalid={errors.icon ? true : undefined}
            className="h-9 rounded-lg border-border bg-background px-md text-body-md"
            {...register("icon")}
          />
          {errors.icon ? (
            <p className="text-label-md text-destructive">
              {errors.icon.message}
            </p>
          ) : null}
        </div>
      </section>

      {/* Buttons */}
      <div className="flex flex-col-reverse gap-sm sm:flex-row sm:justify-end">
        <Link
          href="/dashboard/resources/new"
          className="rounded-lg border border-border px-md py-sm text-center text-label-md text-muted-foreground transition-colors hover:bg-surface-hover"
        >
          Cancel
        </Link>

        <button
          type="submit"
          disabled={isSubmitting}
          className="inline-flex items-center justify-center gap-xs rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors hover:bg-primary/90 disabled:cursor-not-allowed disabled:opacity-70"
        >
          {isSubmitting ? (
            <>
              <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
              Saving…
            </>
          ) : (
            "Create Type"
          )}
        </button>
      </div>
    </form>
  );
}
