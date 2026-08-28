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
  createServiceFormSchema,
  DEFAULT_CREATE_SERVICE_VALUES,
  toCreateServicePayload,
  type CreateServiceFormValues,
} from "@/features/services/schemas/service-schema";
import { createService } from "@/features/services/services/service-api";
import type { Schedule } from "@/features/availability/types/availability";
import { ApiRequestError } from "@/lib/api-error";
import { cn } from "@/lib/utils";

const fieldInputClassName =
  "h-auto rounded-lg border-border bg-background px-md py-sm text-body-md";

const numberInputClassName =
  "w-full rounded-lg border border-border bg-background px-md py-sm text-right text-body-md text-foreground focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none";

const selectClassName =
  "h-9 w-full rounded-lg border border-border bg-background px-md text-body-md text-foreground focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none";

export function CreateServiceForm({ schedules }: { schedules: Schedule[] }) {
  const router = useRouter();
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<CreateServiceFormValues>({
    resolver: zodResolver(createServiceFormSchema),
    mode: "onBlur",
    defaultValues: DEFAULT_CREATE_SERVICE_VALUES,
  });

  async function onSubmit(values: CreateServiceFormValues) {
    setSubmitError(null);

    try {
      await createService(toCreateServicePayload(values));
      router.push("/dashboard/services");
      router.refresh();
    } catch (error) {
      setSubmitError(
        error instanceof ApiRequestError
          ? error.message
          : "Could not create this service. Please try again."
      );
    }
  }

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      noValidate
      className="grid grid-cols-1 gap-lg lg:grid-cols-3"
    >
      <div className="flex flex-col gap-lg lg:col-span-2">
        {submitError ? <AuthFormAlert message={submitError} /> : null}

        <section className="space-y-md rounded-xl border border-border bg-surface p-lg">
          <h2 className="text-headline-md text-foreground">
            Basic Information
          </h2>

          <div className="space-y-base">
            <Label
              htmlFor="title"
              className="text-label-md tracking-wider text-muted-foreground uppercase"
            >
              Service Name
            </Label>
            <Input
              id="title"
              placeholder="e.g., Executive Consultation"
              aria-invalid={errors.title ? true : undefined}
              className={fieldInputClassName}
              {...register("title")}
            />
            {errors.title ? (
              <p className="text-label-md text-destructive">
                {errors.title.message}
              </p>
            ) : null}
          </div>

          <div className="space-y-base">
            <Label
              htmlFor="description"
              className="text-label-md tracking-wider text-muted-foreground uppercase"
            >
              Description
            </Label>
            <textarea
              id="description"
              rows={4}
              placeholder="Briefly describe the service offering..."
              className="w-full resize-none rounded-lg border border-border bg-background px-md py-sm text-body-md text-foreground placeholder:text-muted-foreground focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none"
              {...register("description")}
            />
          </div>

          <div className="grid grid-cols-1 gap-md md:grid-cols-2">
            <div className="space-y-base">
              <Label className="text-label-md tracking-wider text-muted-foreground uppercase">
                Category
              </Label>
              <select
                disabled
                title="core.services has no category column — nothing to select yet."
                className={cn(selectClassName, "cursor-not-allowed opacity-50")}
              >
                <option>Not available yet</option>
              </select>
            </div>

            <div className="space-y-base">
              <Label className="text-label-md tracking-wider text-muted-foreground uppercase">
                Service Cover Image
              </Label>
              <button
                type="button"
                disabled
                title="core.services has no image column, and this platform has no file-upload endpoint yet."
                className="flex h-9 w-full cursor-not-allowed items-center justify-center rounded-lg border border-dashed border-border text-label-md text-muted-foreground opacity-50"
              >
                Upload Image (Max 2MB)
              </button>
            </div>
          </div>

          <div className="space-y-base">
            <Label
              htmlFor="scheduleId"
              className="text-label-md tracking-wider text-muted-foreground uppercase"
            >
              Availability Ruleset
            </Label>
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
              <select
                id="scheduleId"
                aria-invalid={errors.scheduleId ? true : undefined}
                className={selectClassName}
                {...register("scheduleId")}
              >
                <option value="">Select a ruleset…</option>
                {schedules.map((schedule) => (
                  <option key={schedule.scheduleId} value={schedule.scheduleId}>
                    {schedule.title}
                  </option>
                ))}
              </select>
            )}
            {errors.scheduleId ? (
              <p className="text-label-md text-destructive">
                {errors.scheduleId.message}
              </p>
            ) : null}
          </div>
        </section>

        <section className="space-y-md rounded-xl border border-dashed border-border bg-surface/60 p-lg">
          <h2 className="text-headline-md text-muted-foreground">
            Resources &amp; Assignment
          </h2>
          <p className="text-body-md text-muted-foreground">
            Assigning staff to a service is not a direct relationship on this
            platform. A staff member is assigned to a{" "}
            <span className="font-mono text-mono-label">Resource</span>{" "}
            (
            <span className="font-mono text-mono-label">
              resource.resource_assignments
            </span>
            ), and a <span className="font-mono text-mono-label">Resource</span>{" "}
            belongs to a service (
            <span className="font-mono text-mono-label">
              resource.resources.service_id
            </span>
            ). The Resources feature has not been built yet, so this panel is
            reserved rather than faked.
          </p>
        </section>
      </div>

      <div className="flex flex-col gap-lg">
        <section className="space-y-md rounded-xl border border-border bg-surface p-lg">
          <h2 className="text-headline-md text-foreground">
            Pricing &amp; Duration
          </h2>

          <div className="space-y-base">
            <Label className="text-label-md tracking-wider text-muted-foreground uppercase">
              Duration
            </Label>
            <div className="relative">
              <input
                type="number"
                min={1}
                aria-invalid={errors.defaultDuration ? true : undefined}
                className={cn(numberInputClassName, "pr-12")}
                {...register("defaultDuration")}
              />
              <span className="pointer-events-none absolute top-1/2 right-sm -translate-y-1/2 font-mono text-mono-label text-muted-foreground">
                min
              </span>
            </div>
            {errors.defaultDuration ? (
              <p className="text-label-md text-destructive">
                {errors.defaultDuration.message}
              </p>
            ) : null}
          </div>

          <div className="space-y-base">
            <Label
              htmlFor="price"
              className="text-label-md tracking-wider text-muted-foreground uppercase"
            >
              Price
            </Label>
            <div className="flex items-center gap-sm rounded-lg border border-border bg-background px-md">
              <span className="text-body-md text-muted-foreground">$</span>
              <input
                id="price"
                type="text"
                inputMode="decimal"
                placeholder="0.00"
                aria-invalid={errors.price ? true : undefined}
                className="w-full bg-transparent py-sm text-body-md text-foreground outline-none"
                {...register("price")}
              />
              <span className="font-mono text-mono-label text-muted-foreground">
                USD
              </span>
            </div>
            {errors.price ? (
              <p className="text-label-md text-destructive">
                {errors.price.message}
              </p>
            ) : null}
            <p className="text-label-md text-muted-foreground">
              Leave blank for &quot;price on request&quot; (
              <span className="font-mono text-mono-label">TBD</span>).
            </p>
          </div>
        </section>

        <section className="space-y-md rounded-xl border border-border bg-surface p-lg">
          <h2 className="text-headline-md text-foreground">
            Scheduling Rules
          </h2>

          <div className="space-y-base">
            <Label className="text-label-md tracking-wider text-muted-foreground uppercase">
              Buffer Time (Before/After)
            </Label>
            <div className="flex items-center gap-sm">
              <input
                type="number"
                min={0}
                aria-label="Buffer before booking (minutes)"
                className={numberInputClassName}
                {...register("beforeBuffer")}
              />
              <span className="text-muted-foreground">⇄</span>
              <input
                type="number"
                min={0}
                aria-label="Buffer after booking (minutes)"
                className={numberInputClassName}
                {...register("afterBuffer")}
              />
            </div>
            <p className="text-label-md text-muted-foreground">
              Minutes, applied before and after each booking.
            </p>
          </div>

          <div className="grid grid-cols-2 gap-sm">
            <div className="space-y-base">
              <Label className="text-label-md tracking-wider text-muted-foreground uppercase">
                Minimum Notice
              </Label>
              <div className="relative">
                <input
                  type="number"
                  min={0}
                  aria-invalid={errors.minimumNotice ? true : undefined}
                  className={cn(numberInputClassName, "pr-10")}
                  {...register("minimumNotice")}
                />
                <span className="pointer-events-none absolute top-1/2 right-sm -translate-y-1/2 font-mono text-mono-label text-muted-foreground">
                  hrs
                </span>
              </div>
              {errors.minimumNotice ? (
                <p className="text-label-md text-destructive">
                  {errors.minimumNotice.message}
                </p>
              ) : null}
            </div>

            <div className="space-y-base">
              <Label className="text-label-md tracking-wider text-muted-foreground uppercase">
                Max Advance
              </Label>
              <div className="relative">
                <input
                  type="number"
                  min={1}
                  aria-invalid={errors.maximumAdvanceBooking ? true : undefined}
                  className={cn(numberInputClassName, "pr-10")}
                  {...register("maximumAdvanceBooking")}
                />
                <span className="pointer-events-none absolute top-1/2 right-sm -translate-y-1/2 font-mono text-mono-label text-muted-foreground">
                  days
                </span>
              </div>
              {errors.maximumAdvanceBooking ? (
                <p className="text-label-md text-destructive">
                  {errors.maximumAdvanceBooking.message}
                </p>
              ) : null}
            </div>
          </div>

          <div className="space-y-base">
            <Label className="text-label-md tracking-wider text-muted-foreground uppercase">
              Capacity
            </Label>
            <input
              type="number"
              min={1}
              aria-invalid={errors.capacity ? true : undefined}
              className={numberInputClassName}
              {...register("capacity")}
            />
            <p className="text-label-md text-muted-foreground">
              Attendees allowed per booking slot.
            </p>
            {errors.capacity ? (
              <p className="text-label-md text-destructive">
                {errors.capacity.message}
              </p>
            ) : null}
          </div>

          <div className="space-y-base">
            <Label className="text-label-md tracking-wider text-muted-foreground uppercase">
              Booking Window
            </Label>
            <select className={selectClassName} {...register("bookingWindowType")}>
              <option value="ROLLING">Rolling (N days from today)</option>
              <option value="FIXED">Fixed (a set date range)</option>
            </select>
          </div>

          <div className="space-y-base">
            <Label className="text-label-md tracking-wider text-muted-foreground uppercase">
              Visibility Policy
            </Label>
            <select
              disabled
              title="core.services has no visibility column — nothing to select yet."
              className={cn(selectClassName, "cursor-not-allowed opacity-50")}
            >
              <option>Not available yet</option>
            </select>
          </div>
        </section>

        <button
          type="submit"
          disabled={isSubmitting || schedules.length === 0}
          className={cn(
            "flex items-center justify-center gap-xs rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors",
            "hover:bg-primary/90",
            "disabled:cursor-not-allowed disabled:opacity-70"
          )}
        >
          {isSubmitting ? (
            <>
              <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
              Saving…
            </>
          ) : (
            "Save Service"
          )}
        </button>
      </div>
    </form>
  );
}
